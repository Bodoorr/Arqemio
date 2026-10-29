package com.ga.arqemio.service;

import com.ga.arqemio.model.*;
import com.ga.arqemio.model.request.ProjectUpdateShareRequest;
import com.ga.arqemio.model.response.ProjectUpdateShareResponse;
import com.ga.arqemio.repository.*;
import com.ga.arqemio.security.MyUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@AllArgsConstructor
public class ProjectShareLinkService {
    private ProjectShareLinkRepository projectShareLinkRepository;
    private ProjectUpdateRepository projectUpdateRepository;
    private ProjectRepository projectRepository;
    private CompanyMembershipRepository companyMembershipRepository;
    private EmailService emailService;
    private FileAttachmentRepository fileAttachmentRepository;
    private AuditLogService auditLogService;
    private NotificationService notificationService;
    private UserService userService;

    private static final Logger logger =Logger.getLogger(ProjectShareLinkService.class.getName());

    public static User getCurrentLoggedInUser(){
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    public String getProjectShareEmail(String projectName, String message, String companyName, String shareLink, String registrationLink, List<FileAttachment> attachments) {
        try {
            ClassPathResource resource =
                    new ClassPathResource("templates/project-share-email.html");

            String html = new String(
                    resource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8);
            StringBuilder imagesHtml = new StringBuilder();
            for (FileAttachment attachment : attachments) {
                if (attachment.getFileType() != null &&
                        attachment.getFileType().startsWith("image/")) {
                    imagesHtml.append(
                            "<img src=\"" + attachment.getFileUrl() +
                                    "\" class=\"email-image\" " +
                                    "style=\"display:block;width:100%;max-width:100%;height:auto;" +
                                    "border-radius:10px;margin:10px 0;\" " +
                                    "alt=\"Construction Progress\" />"
                    );
                }
            }
            return html
                    .replace("{{projectName}}", projectName)
                    .replace("{{message}}", message)
                    .replace("{{companyName}}", companyName)
                    .replace("{{shareLink}}", shareLink)
                    .replace("{{registrationLink}}", registrationLink)
                    .replace("{{images}}", imagesHtml.toString());

        } catch (IOException e) {
            throw new RuntimeException("Could not load project share email.");
        }
    }

    public ProjectShareLink shareProjectUpdate(Long updateId, ProjectUpdateShareRequest projectUpdateShareRequest){
        User currentUser= getCurrentLoggedInUser();
        ProjectUpdate projectUpdate= projectUpdateRepository.findById(updateId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project Update not found."));
        Project project= projectUpdate.getProject();

        if (!canShareProjectUpdate(currentUser, project)){
            logger.log(Level.WARNING,"User {0} attempted to share project update {1} without permission", new Object[]{currentUser.getId(), updateId});
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to share this project update");
        }

        if (!projectUpdate.getStatus().equals("APPROVED")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Only approved project updates can be shared.");
        }

        CompanyMembership membership= companyMembershipRepository.findByUserIdAndCompanyIdAndStatus(currentUser.getId(), project.getCompany().getId(),"ACTIVE").orElseThrow(()-> new ResponseStatusException(HttpStatus.FORBIDDEN,"Active company membership not found."));

        ProjectShareLink shareLink=new ProjectShareLink();
        shareLink.setProjectUpdate(projectUpdate);
        shareLink.setCreatedBy(membership);
        shareLink.setCustomerEmail(projectUpdateShareRequest.getCustomerEmail());
        shareLink.setSubject(projectUpdateShareRequest.getSubject());
        shareLink.setMessage(projectUpdateShareRequest.getMessage());
        shareLink.setToken(UUID.randomUUID().toString());
        shareLink.setExpiresAt(LocalDateTime.now().plusHours(24));

        ProjectShareLink savedShareLink = projectShareLinkRepository.save(shareLink);

        String link= "http://localhost:5173/project-preview/"+ shareLink.getToken();
        String registrationLink= "http://localhost:5173/customer/register/"+ savedShareLink.getToken();
        List<FileAttachment> attachments = fileAttachmentRepository.findByProjectUpdateIdAndStatus(projectUpdate.getId(), "ACTIVE");
        String emailBody = getProjectShareEmail(project.getName(), shareLink.getMessage(), project.getCompany().getName(), link, registrationLink, attachments);

        EmailDetails emailDetails = new EmailDetails(
                shareLink.getCustomerEmail(),
                emailBody,
                shareLink.getSubject(),
                null
        );

        emailService.sendHtmlMail(emailDetails);
        notificationService.sendNotification(projectUpdate.getUpdatedBy().getUser().getId(), "Your project update "+projectUpdate.getTitle()+" has been shared with the customer.");
        auditLogService.createAuditLog(currentUser, project.getCompany(),
                "SHARE", "PROJECT_UPDATE", projectUpdate.getId(),
                "Shared project update with customer " + shareLink.getCustomerEmail()
        );
        logger.log(Level.INFO,"Project share link {0} created for project update {1} by user {2}", new Object[]{savedShareLink.getId(), updateId, currentUser.getId()});
        return savedShareLink;
    }

    public ProjectUpdateShareResponse getSharedProjectUpdate(String token){
        ProjectShareLink shareLink= projectShareLinkRepository.findByToken(token).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project share link not found."));

        if (shareLink.getRevokedAt() !=null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This project share link has been revoked.");
        }

        if (shareLink.getExpiresAt().isBefore(LocalDateTime.now())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This project share link has expired.");
        }

        ProjectUpdate projectUpdate= shareLink.getProjectUpdate();

        List<FileAttachment> attachments= fileAttachmentRepository.findByProjectUpdateIdAndStatus(projectUpdate.getId(),"ACTIVE");
        List<String> images= new ArrayList<>();

        for (FileAttachment attachment: attachments){
            images.add(attachment.getFileUrl());
        }

        return new ProjectUpdateShareResponse(
                projectUpdate.getProject().getCompany().getName(),
                projectUpdate.getProject().getName(),
                projectUpdate.getTitle(),
                projectUpdate.getDescription(),
                projectUpdate.getCreatedAt(),
                images,
                shareLink.getExpiresAt()
        );
    }

    public String getCustomerRegistrationEmail(String token){
        ProjectShareLink shareLink= projectShareLinkRepository.findByToken(token).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Registration link not found."));
        if (shareLink.getRevokedAt()!=null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This registration link has been revoked");
        }
        return shareLink.getCustomerEmail();
    }

    public User registerCustomer(String token, String name, String password){
        String email= getCustomerRegistrationEmail(token);
        return userService.registerCustomer(name,email,password);
    }

    public List<ProjectUpdateShareResponse> getCustomerSharedUpdates(){
        User currentUser= getCurrentLoggedInUser();

        if (!Boolean.TRUE.equals(currentUser.getIsCustomer())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only customers can access this dashboard.");
        }

        List<ProjectShareLink> shareLinks= projectShareLinkRepository.findByCustomerEmailIgnoreCaseAndRevokedAtIsNull(currentUser.getEmail());
        List<ProjectUpdateShareResponse> responses=new ArrayList<>();

        for (ProjectShareLink shareLink: shareLinks){
            ProjectUpdate projectUpdate= shareLink.getProjectUpdate();

            List<FileAttachment> attachments=fileAttachmentRepository.findByProjectUpdateIdAndStatus(projectUpdate.getId(),"ACTIVE");
            List<String> images= new ArrayList<>();

            for (FileAttachment attachment: attachments){
                images.add(attachment.getFileUrl());
            }

            responses.add(new ProjectUpdateShareResponse(
                    projectUpdate.getProject().getCompany().getName(),
                    projectUpdate.getProject().getName(),
                    projectUpdate.getTitle(),
                    projectUpdate.getDescription(),
                    projectUpdate.getCreatedAt(),
                    images,
                    shareLink.getExpiresAt()
            ));
        }
        return responses;
    }

    private boolean canShareProjectUpdate(User user, Project project){
        boolean isCompanyOwner= companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(user.getId(), project.getCompany().getId(),"OWNER","ACTIVE");
        boolean isAssignedManager= projectRepository.existsByIdAndManagersUserIdAndManagersStatus(project.getId(), user.getId(), "ACTIVE");

        return isCompanyOwner || isAssignedManager;
    }

}
