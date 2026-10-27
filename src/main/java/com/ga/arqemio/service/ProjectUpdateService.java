package com.ga.arqemio.service;

import com.ga.arqemio.model.CompanyMembership;
import com.ga.arqemio.model.Project;
import com.ga.arqemio.model.ProjectUpdate;
import com.ga.arqemio.model.User;
import com.ga.arqemio.model.request.ProjectUpdateRequest;
import com.ga.arqemio.repository.CompanyMembershipRepository;
import com.ga.arqemio.repository.ProjectRepository;
import com.ga.arqemio.repository.ProjectUpdateRepository;
import com.ga.arqemio.security.MyUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@AllArgsConstructor
public class ProjectUpdateService {
    private ProjectUpdateRepository projectUpdateRepository;
    private ProjectRepository projectRepository;
    private CompanyMembershipRepository companyMembershipRepository;
    private NotificationService notificationService;
    private AuditLogService auditLogService;
    private static final Logger logger= Logger.getLogger(ProjectUpdateService.class.getName());

    public static User getCurrentLoggedInUser(){
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    public ProjectUpdate createProjectUpdate(ProjectUpdateRequest projectUpdateRequest){
        User currentUser= getCurrentLoggedInUser();
        Project project= projectRepository.findById(projectUpdateRequest.getProjectId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
        CompanyMembership membership= companyMembershipRepository.findByUserIdAndCompanyIdAndStatus(currentUser.getId(), project.getCompany().getId(), "ACTIVE")
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.FORBIDDEN,"Active company membership not found."));

        if (!membership.getRole().equals("WORKER")){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only workers can create project updates.");
        }

        boolean isAssignedWorker= projectRepository.existsByIdAndWorkersId(project.getId(),membership.getId());

        if (!isAssignedWorker){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You're not assigned to this project.");
        }

        ProjectUpdate projectUpdate=new ProjectUpdate();
        projectUpdate.setProject(project);
        projectUpdate.setUpdatedBy(membership);
        projectUpdate.setTitle(projectUpdateRequest.getTitle());
        projectUpdate.setDescription(projectUpdateRequest.getDescription());
        projectUpdate.setStatus("PENDING");

        ProjectUpdate savedProjectUpdate = projectUpdateRepository.save(projectUpdate);
        auditLogService.createAuditLog(currentUser,project.getCompany(),"CREATE","PROJECT_UPDATE",savedProjectUpdate.getId(),"Submitted project update: "+savedProjectUpdate.getTitle());
        logger.log(Level.INFO,"Project update {0} created for project {1} by user {2}", new Object[]{savedProjectUpdate.getId(), project.getId(), currentUser.getId()});
        for (CompanyMembership manager: project.getManagers()){
            notificationService.sendNotification(manager.getUser().getId(), "New project update submitted for "+ project.getName());
        }
        return savedProjectUpdate;
    }

    public List<ProjectUpdate> getProjectUpdates(Long projectId){
        User currentUser= getCurrentLoggedInUser();
        Project project= projectRepository.findById(projectId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));

        if (!canViewProjectUpdate(currentUser, project)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You're not allowed to view updates for this project.");
        }

        return projectUpdateRepository.findByProjectId(projectId);
    }

    public ProjectUpdate getProjectUpdateById(Long updateId){
        User currentUser= getCurrentLoggedInUser();
        ProjectUpdate projectUpdate= findProjectUpdateById(updateId);

        if (!canViewProjectUpdate(currentUser, projectUpdate.getProject())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to view this project update.");
        }

        return projectUpdate;
    }

    public ProjectUpdate reviewProjectUpdate(Long updateId, String status, String reviewNote){
        User currentUser= getCurrentLoggedInUser();
        ProjectUpdate projectUpdate= findProjectUpdateById(updateId);
        Project project= projectUpdate.getProject();
        if (!canManageProjectUpdate(currentUser, project)){
            logger.log(Level.WARNING, "User {0} attempted to review project update {1} without permission", new Object[]{currentUser.getId(), updateId});
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to review this project update.");
        }

        if (!projectUpdate.getStatus().equals("PENDING")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This project update has already been reviewed.");
        }

        if (!status.equals("APPROVED") && !status.equals("REJECTED")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be APPROVED or REJECTED.");
        }

        CompanyMembership reviewer= companyMembershipRepository.findByUserIdAndCompanyIdAndStatus(currentUser.getId(), project.getCompany().getId(), "ACTIVE")
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.FORBIDDEN, "Active company membership not found."));

        projectUpdate.setStatus(status);
        projectUpdate.setReviewedBy(reviewer);
        projectUpdate.setReviewNote(reviewNote);

        ProjectUpdate reviewedUpdate= projectUpdateRepository.save(projectUpdate);
        auditLogService.createAuditLog(currentUser, project.getCompany(),status.equals("APPROVED")? "APPROVE" : "REJECT", "PROJECT_UPDATE", reviewedUpdate.getId(),"Reviewed project update: "+reviewedUpdate.getTitle()+" - "+status);
        notificationService.sendNotification(reviewedUpdate.getUpdatedBy().getUser().getId(), "Your project update "+reviewedUpdate.getTitle()+" has been "+status.toLowerCase());
        logger.log(Level.INFO, "Project update {0} reviewed with status {1} by user {2}", new Object[]{updateId, status,currentUser.getId()});
        return reviewedUpdate;
    }

    public ProjectUpdate updateProjectUpdate(Long updateId, ProjectUpdateRequest projectUpdateRequest){
        User currentUser= getCurrentLoggedInUser();

        ProjectUpdate projectUpdate= findProjectUpdateById(updateId);

        if (!projectUpdate.getUpdatedBy().getUser().getId().equals(currentUser.getId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to update this project update.");
        }

        if (!projectUpdate.getStatus().equals("PENDING")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending project updates can be updated.");
        }

        projectUpdate.setTitle(projectUpdateRequest.getTitle());
        projectUpdate.setDescription(projectUpdateRequest.getDescription());

        ProjectUpdate updatedProjectUpdate= projectUpdateRepository.save(projectUpdate);
        auditLogService.createAuditLog(currentUser, updatedProjectUpdate.getProject().getCompany(),"UPDATE","PROJECT_UPDATE", updatedProjectUpdate.getId(), "Updated project update: "+updatedProjectUpdate.getTitle());
        logger.log(Level.INFO, "Project update {0} edited by user {1}", new Object[]{updateId, currentUser.getId()});
        return updatedProjectUpdate;
    }

    public ProjectUpdate archiveProjectUpdate(Long updateId){
        User currentUser= getCurrentLoggedInUser();
        ProjectUpdate projectUpdate= findProjectUpdateById(updateId);

        if (!projectUpdate.getUpdatedBy().getUser().getId().equals(currentUser.getId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to archive this project update.");
        }

        if (!projectUpdate.getStatus().equals("PENDING")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Only pending project updates can be archived.");
        }

        projectUpdate.setStatus("ARCHIVED");

        ProjectUpdate archivedUpdate= projectUpdateRepository.save(projectUpdate);
        auditLogService.createAuditLog(currentUser, archivedUpdate.getProject().getCompany(),"ARCHIVE","PROJECT_UPDATE", archivedUpdate.getId(), "Archived project update: "+archivedUpdate.getTitle());
        logger.log(Level.INFO,"Project update {0} archived by user {1}", new Object[]{updateId, currentUser.getId()});
        return archivedUpdate;
    }

    private ProjectUpdate findProjectUpdateById(Long updateId){
        return projectUpdateRepository.findById(updateId)
                .orElseThrow(()-> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Project update not found."));
    }

    private boolean canManageProjectUpdate(User user, Project project){
        boolean isCompanyOwner= companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(user.getId(),project.getCompany().getId(), "OWNER", "ACTIVE");
        boolean isAssignedManager= projectRepository.existsByIdAndManagersUserIdAndManagersStatus(project.getId(), user.getId(), "ACTIVE");

        return isAssignedManager || isCompanyOwner;
    }

    private boolean canViewProjectUpdate(User user, Project project){
        boolean canManage= canManageProjectUpdate(user,project);
        boolean isAssignedWorker = projectRepository.existsByIdAndWorkersUserIdAndWorkersStatus(project.getId(), user.getId(), "ACTIVE");

        return canManage || isAssignedWorker;
    }
}
