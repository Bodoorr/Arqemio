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
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@AllArgsConstructor
public class ProjectService {
    private final Logger logger= Logger.getLogger(ProjectService.class.getName());
    private ProjectRepository projectRepository;
    private CompanyRepository companyRepository;
    private CompanyMembershipRepository companyMembershipRepository;
    private AuditLogService auditLogService;

    public static User getCurrentLoggedInUser(){
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    public Project createProject(ProjectRequest projectRequest){
        User currentUser= getCurrentLoggedInUser();
        Company company= companyRepository.findById(projectRequest.getCompanyId()).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found."));

        if (!isCompanyOwner(currentUser, company.getId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to create projects for this company.");
        }

        validateProjectDates(projectRequest);

        Project project= new Project();
        project.setCompany(company);
        setProjectFields(project,projectRequest);

        Project savedProject = projectRepository.save(project);

        logger.log(Level.INFO,"Project {0} created successfully", savedProject.getId());

        auditLogService.createAuditLog(currentUser, company,
                "CREATE", "PROJECT",
                savedProject.getId(), "Created project " + savedProject.getName());

        return savedProject;

    }

    public List<Project> getAllProjects(){
        User currentUser= getCurrentLoggedInUser();
        return projectRepository.findByCompanyMembershipsUserIdAndCompanyMembershipsStatus(currentUser.getId(), "ACTIVE");
    }

    public Page<Project> getProjects(Pageable pageable, String name, String status) {
        User currentUser= getCurrentLoggedInUser();
        if (name!=null && !name.isBlank()){
            return projectRepository.findByCompanyMembershipsUserIdAndCompanyMembershipsStatusAndNameContainingIgnoreCase
                    (currentUser.getId(), "ACTIVE", name, pageable);
        }
        if (status!=null && !status.isBlank()){
            return projectRepository.findByCompanyMembershipsUserIdAndCompanyMembershipsStatusAndStatus
                    (currentUser.getId(), "ACTIVE", status, pageable);
        }

        return projectRepository.findByCompanyMembershipsUserIdAndCompanyMembershipsStatus(currentUser.getId(),"ACTIVE",pageable);
    }


    public Project getProjectById(Long projectId){
        User currentUser= getCurrentLoggedInUser();
        Project project= findProjectById(projectId);
        boolean isActiveMember= companyMembershipRepository.existsByUserIdAndCompanyIdAndStatus(currentUser.getId(), project.getCompany().getId(), "ACTIVE");

         if (!isActiveMember){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to view this project.");
        }
        return project;
    }

    public Project updateProject(Long projectId, ProjectRequest projectRequest){
        User currentUser= getCurrentLoggedInUser();
        Project project= findProjectById(projectId);

        if (!isCompanyOwner(currentUser, project.getCompany().getId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to update this project.");
        }

        validateProjectDates(projectRequest);
        setProjectFields(project,projectRequest);

        return projectRepository.save(project);

    }

    public Project archiveProject(Long projectId){
        User currentUser= getCurrentLoggedInUser();
        Project project= findProjectById(projectId);

        if (!isCompanyOwner(currentUser, project.getCompany().getId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to delete this project.");
        }
        project.setStatus("ARCHIVED");
        return projectRepository.save(project);
    }


    public CompanyMembership assignManager(Long projectId, Long membershipId){
        User currentUser= getCurrentLoggedInUser();
        Project project= findProjectById(projectId);
        CompanyMembership membership= findMembershipById(membershipId);

        if (!isCompanyOwner(currentUser, project.getCompany().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to assign managers to this project.");
        }

        if (!membership.getCompany().getId().equals(project.getCompany().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This member does not belong to the project's company.");
        }

        if (!membership.getRole().equals("MANAGER")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This member is not a manager."
            );
        }

        if (!membership.getStatus().equals("ACTIVE")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This manager is not active.");
        }

        if (project.getManagers().contains(membership)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This manager is already assigned to this project.");
        }
        project.getManagers().add(membership);
        projectRepository.save(project);

        return membership;
    }

    public CompanyMembership removeManager(Long projectId, Long membershipId){
        User currentUser= getCurrentLoggedInUser();
        Project project= findProjectById(projectId);
        CompanyMembership membership= findMembershipById(membershipId);

        if (!isCompanyOwner(currentUser, project.getCompany().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to remove managers from this project.");
        }

        if (!project.getManagers().contains(membership)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This manager is not assigned to this project.");
        }

        project.getManagers().remove(membership);
        projectRepository.save(project);

        return membership;
    }

    public CompanyMembership assignWorker(Long projectId, Long membershipId){
        User currentUser= getCurrentLoggedInUser();
        Project project= findProjectById(projectId);
        CompanyMembership membership= findMembershipById(membershipId);

        if (!isCompanyOwner(currentUser, project.getCompany().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to assign workers to this project.");
        }

        if (!membership.getCompany().getId().equals(project.getCompany().getId())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This member does not belong to the project's company.");
        }

        if (!membership.getRole().equals("WORKER")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This member is not a worker.");
        }

        if (!membership.getStatus().equals("ACTIVE")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This worker is not active.");
        }

        if (project.getWorkers().contains(membership)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This worker is already assigned to this project.");
        }

        project.getWorkers().add(membership);
        projectRepository.save(project);

        return membership;
    }

    public CompanyMembership removeWorker(Long projectId, Long membershipId){
        User currentUser= getCurrentLoggedInUser();
        Project project= findProjectById(projectId);
        CompanyMembership membership= findMembershipById(membershipId);

        if (!isCompanyOwner(currentUser, project.getCompany().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to remove workers from this project.");
        }

        if (!project.getWorkers().contains(membership)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This worker is not assigned to this project.");
        }

        project.getWorkers().remove(membership);
        projectRepository.save(project);

        return membership;
    }

    private Project findProjectById(Long projectId){
        return projectRepository.findById(projectId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
    }

    private CompanyMembership findMembershipById(Long membershipId){
        return companyMembershipRepository.findById(membershipId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membership not found."));
    }

    private void validateProjectDates(ProjectRequest projectRequest){
        if (projectRequest.getStartDate() == null || projectRequest.getExpectedEndDate()== null || projectRequest.getExpectedEndDate().isBefore(projectRequest.getStartDate())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expected end date cannot be before start date.");
        }
    }

    private void setProjectFields(Project project, ProjectRequest projectRequest){
        project.setName(projectRequest.getName());
        project.setDescription(projectRequest.getDescription());
        project.setLocation(projectRequest.getLocation());
        project.setStartDate(projectRequest.getStartDate());
        project.setExpectedEndDate(projectRequest.getExpectedEndDate());
        project.setBudget(projectRequest.getBudget());
        project.setStatus(projectRequest.getStatus());
    }

    private boolean isCompanyOwner(User user, Long companyId){
        return companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(user.getId(), companyId,"OWNER","ACTIVE");
    }

}
