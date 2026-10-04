package com.ga.arqemio.service;

import com.ga.arqemio.model.Company;
import com.ga.arqemio.model.CompanyMembership;
import com.ga.arqemio.model.Project;
import com.ga.arqemio.model.User;
import com.ga.arqemio.model.request.ProjectRequest;
import com.ga.arqemio.repository.CompanyMembershipRepository;
import com.ga.arqemio.repository.CompanyRepository;
import com.ga.arqemio.repository.ProjectRepository;
import com.ga.arqemio.security.MyUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ProjectService {
    private ProjectRepository projectRepository;
    private CompanyRepository companyRepository;
    private CompanyMembershipRepository companyMembershipRepository;

    public static User getCurrentLoggedInUser(){
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    public Project createProject(ProjectRequest projectRequest){
        User currentUser= getCurrentLoggedInUser();
        Company company= companyRepository.findById(projectRequest.getCompanyId()).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found."));
        boolean isPlatformAdmin = currentUser.getIsPlatformAdmin().equals(true);

        boolean isCompanyOwner =
                companyMembershipRepository
                        .existsByUserIdAndCompanyIdAndRoleAndStatus(
                                currentUser.getId(),
                                company.getId(),
                                "OWNER",
                                "ACTIVE"
                        );

        if (!isPlatformAdmin && !isCompanyOwner){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to create projects for this company.");
        }

        if (projectRequest.getExpectedEndDate().isBefore(projectRequest.getStartDate())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expected end date cannot be before start date.");
        }

        Project project= new Project();
        project.setCompany(company);
        project.setName(projectRequest.getName());
        project.setDescription(projectRequest.getDescription());
        project.setLocation(projectRequest.getLocation());
        project.setStartDate(projectRequest.getStartDate());
        project.setExpectedEndDate(projectRequest.getExpectedEndDate());
        project.setBudget(projectRequest.getBudget());
        project.setStatus(projectRequest.getStatus());


        return projectRepository.save(project);

    }

    public List<Project> getAllProjects(){
        User currentUser= getCurrentLoggedInUser();
        if (currentUser.getIsPlatformAdmin()) {
            return projectRepository.findAll();
        }

        return projectRepository.findByCompanyMembershipsUserIdAndCompanyMembershipsStatus(currentUser.getId(), "ACTIVE");

    }


}
