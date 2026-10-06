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

@Service
@AllArgsConstructor
public class ProjectUpdateService {
    private ProjectUpdateRepository projectUpdateRepository;
    private ProjectRepository projectRepository;
    private CompanyMembershipRepository companyMembershipRepository;

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

        return projectUpdateRepository.save(projectUpdate);

    }

}
