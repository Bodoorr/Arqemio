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

@Service
@AllArgsConstructor
public class ProjectUpdateService {
    private ProjectUpdateRepository projectUpdateRepository;
    private ProjectRepository projectRepository;
    private CompanyMembershipRepository companyMembershipRepository;
    private NotificationService notificationService;

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
        for (CompanyMembership manager: project.getManagers()){
            notificationService.sendNotification(manager.getUser().getId(), "New project update submitted for "+ project.getName());
        }
        return savedProjectUpdate;
    }

    public List<ProjectUpdate> getProjectUpdates(Long projectId){
        User currentUser= getCurrentLoggedInUser();
        Project project= projectRepository.findById(projectId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
        boolean isPlatformAdmin= currentUser.getIsPlatformAdmin().equals(true);
        boolean isCompanyOwner = companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(currentUser.getId(), project.getCompany().getId(), "OWNER", "ACTIVE");
        boolean isAssignedManager = projectRepository.existsByIdAndManagersUserIdAndManagersStatus(projectId, currentUser.getId(), "ACTIVE");
        boolean isAssignedWorker = projectRepository.existsByIdAndWorkersUserIdAndWorkersStatus(projectId, currentUser.getId(), "ACTIVE");

        if (!isPlatformAdmin && !isCompanyOwner && !isAssignedManager && !isAssignedWorker){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You're not allowed to view updates for this project.");
        }

        return projectUpdateRepository.findByProjectId(projectId);
    }

    public ProjectUpdate getProjectUpdateById(Long updateId){
        User currentUser= getCurrentLoggedInUser();
        ProjectUpdate projectUpdate= projectUpdateRepository.findById(updateId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Project update not found."));
        boolean isPlatformAdmin= currentUser.getIsPlatformAdmin().equals(true);
        boolean isCompanyOwner = companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(currentUser.getId(), projectUpdate.getProject().getCompany().getId(), "OWNER", "ACTIVE");
        boolean isAssignedManager = projectRepository.existsByIdAndManagersUserIdAndManagersStatus(projectUpdate.getProject().getId(), currentUser.getId(), "ACTIVE");
        boolean isAssignedWorker = projectRepository.existsByIdAndWorkersUserIdAndWorkersStatus(projectUpdate.getProject().getId(), currentUser.getId(), "ACTIVE");

        if (!isPlatformAdmin && !isCompanyOwner && !isAssignedManager && !isAssignedWorker) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to view this project update.");
        }

        return projectUpdate;
    }

    public ProjectUpdate reviewProjectUpdate(Long updateId, String status, String reviewNote){
        User currentUser= getCurrentLoggedInUser();
        ProjectUpdate projectUpdate= projectUpdateRepository.findById(updateId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project update not found."));
        Project project= projectUpdate.getProject();
        boolean isCompanyOwner= companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(currentUser.getId(),project.getCompany().getId(), "OWNER", "ACTIVE");
        boolean isAssignedManager= projectRepository.existsByIdAndManagersUserIdAndManagersStatus(project.getId(), currentUser.getId(), "ACTIVE");

        if (!isCompanyOwner && !isAssignedManager){
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


        return projectUpdateRepository.save(projectUpdate);
    }

    public ProjectUpdate updateProjectUpdate(Long updateId, ProjectUpdateRequest projectUpdateRequest){
        User currentUser= getCurrentLoggedInUser();

        ProjectUpdate projectUpdate= projectUpdateRepository.findById(updateId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Project update not found."));

        if (!projectUpdate.getUpdatedBy().getUser().getId().equals(currentUser.getId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to update this project update.");
        }

        if (!projectUpdate.getStatus().equals("PENDING")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending project updates can be updated.");
        }

        projectUpdate.setTitle(projectUpdateRequest.getTitle());
        projectUpdate.setDescription(projectUpdateRequest.getDescription());

        return projectUpdateRepository.save(projectUpdate);
    }

    public ProjectUpdate archiveProjectUpdate(Long updateId){
        User currentUser= getCurrentLoggedInUser();
        ProjectUpdate projectUpdate= projectUpdateRepository.findById(updateId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project update not found."));

        if (!projectUpdate.getUpdatedBy().getUser().getId().equals(currentUser.getId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to archive this project update.");
        }

        if (!projectUpdate.getStatus().equals("PENDING")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Only pending project updates can be archived.");
        }

        projectUpdate.setStatus("ARCHIVED");

        return projectUpdateRepository.save(projectUpdate);
    }
}
