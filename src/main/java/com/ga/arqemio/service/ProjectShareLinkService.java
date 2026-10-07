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

    public static User getCurrentLoggedInUser(){
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    public String getProjectShareEmail(String projectName, String message, String companyName, String shareLink) {
        try {
            ClassPathResource resource =
                    new ClassPathResource("templates/project-share-email.html");

            String html = new String(
                    resource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8);
            html = html.replace("{{projectName}}", projectName);
            html = html.replace("{{message}}", message);
            html = html.replace("{{companyName}}", companyName);
            html = html.replace("{{shareLink}}", shareLink);

            return html;

        } catch (IOException e) {
            throw new RuntimeException("Could not load project share email.");
        }
    }

    public ProjectShareLink shareProjectUpdate(Long updateId, ProjectUpdateShareRequest projectUpdateShareRequest){
        User currentUser= getCurrentLoggedInUser();
        ProjectUpdate projectUpdate= projectUpdateRepository.findById(updateId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project Update not found."));
        Project project= projectUpdate.getProject();
        boolean isCompanyOwner= companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(currentUser.getId(), project.getCompany().getId(),"OWNER","ACTIVE");
        boolean isAssignedManager= projectRepository.existsByIdAndManagersUserIdAndManagersStatus(project.getId(), currentUser.getId(), "ACTIVE");

        if (!isCompanyOwner && !isAssignedManager){
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

        String emailBody = getProjectShareEmail(project.getName(), shareLink.getMessage(), project.getCompany().getName(), link);

        EmailDetails emailDetails = new EmailDetails(
                shareLink.getCustomerEmail(),
                emailBody,
                shareLink.getSubject(),
                null
        );

        emailService.sendHtmlMail(emailDetails);

        auditLogService.createAuditLog(currentUser, project.getCompany(),
                "SHARE", "PROJECT_UPDATE", projectUpdate.getId(),
                "Shared project update with customer " + shareLink.getCustomerEmail()
        );

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

        ProjectUpdateShareResponse projectUpdateShareResponse=new ProjectUpdateShareResponse(
                projectUpdate.getProject().getCompany().getName(),
                projectUpdate.getProject().getName(),
                projectUpdate.getTitle(),
                projectUpdate.getDescription(),
                projectUpdate.getCreatedAt(),
                images,
                shareLink.getExpiresAt()
        );
        return projectUpdateShareResponse;
    }

}
