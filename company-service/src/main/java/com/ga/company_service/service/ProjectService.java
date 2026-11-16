package com.ga.company_service.service;

import com.ga.company_service.event.ArqemioEvent;
import com.ga.company_service.kafka.ArqemioEventProducer;
import com.ga.company_service.model.Company;
import com.ga.company_service.model.CompanyMembership;
import com.ga.company_service.model.Project;
import com.ga.company_service.model.request.ProjectRequest;
import com.ga.company_service.model.response.UserResponse;
import com.ga.company_service.repository.CompanyMembershipRepository;
import com.ga.company_service.repository.CompanyRepository;
import com.ga.company_service.repository.ProjectRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@AllArgsConstructor
public class ProjectService {
    private static final Logger logger= Logger.getLogger(ProjectService.class.getName());
    private ProjectRepository projectRepository;
    private CompanyRepository companyRepository;
    private CompanyMembershipRepository companyMembershipRepository;
    private ArqemioEventProducer arqemioEventProducer;
//    private AuditLogService auditLogService;
//    private NotificationService notificationService;

    public static UserResponse getCurrentLoggedInUser(){
        return (UserResponse) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public Project createProject(ProjectRequest projectRequest){
        UserResponse currentUser= getCurrentLoggedInUser();
        Company company= companyRepository.findById(projectRequest.getCompanyId()).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found."));

        if (!isCompanyOwner(currentUser, company.getId())){
            logger.log(Level.WARNING,"User {0} attempted to create a project for company {1} without permission", new Object[]{currentUser.getId(), company.getId()});
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to create projects for this company.");
        }

        validateProjectDates(projectRequest);

        Project project= new Project();
        project.setCompany(company);
        setProjectFields(project,projectRequest);

        Project savedProject = projectRepository.save(project);

        logger.log(Level.INFO,"Project {0} created successfully", savedProject.getId());
        ArqemioEvent event = new ArqemioEvent(
                UUID.randomUUID().toString(),
                currentUser.getId(),
                company.getId(),
                "CREATE",
                "PROJECT",
                savedProject.getId(),
                "Created project " + savedProject.getName(),
                LocalDateTime.now()
        );

        arqemioEventProducer.sendEvent(event);
//        auditLogService.createAuditLog(currentUser, company,
//                "CREATE", "PROJECT",
//                savedProject.getId(), "Created project " + savedProject.getName());

        return savedProject;

    }


    public List<Project> getAllProjects(){
        UserResponse currentUser= getCurrentLoggedInUser();
        return getAllProjects(currentUser.getId());
    }

    public List<Project> getAllProjects(Long userId){
        List<Project> projects= new ArrayList<>();
        List<CompanyMembership> memberships= companyMembershipRepository.findByUserIdAndStatus(userId, "ACTIVE");

        for (CompanyMembership membership: memberships){
            List<Project> accessibleProjects= new ArrayList<>();

            if (membership.getRole().equals("OWNER")){
                accessibleProjects= projectRepository.findByCompanyId(membership.getCompany().getId());
            }

            if (membership.getRole().equals("MANAGER")){
                accessibleProjects= projectRepository.findByManagersId(membership.getId());
            }

            if (membership.getRole().equals("WORKER")){
                accessibleProjects= projectRepository.findByWorkersId(membership.getId());
            }

            for (Project project: accessibleProjects){
                if (!projects.contains(project)){
                    projects.add(project);
                }
            }
        }
        return projects;
    }


    public Page<Project> getProjects(Pageable pageable, String name, String status) {
       List<Project> projects= getAllProjects();
       List<Long> projectIds= new ArrayList<>();

       for (Project project: projects){
           projectIds.add(project.getId());
       }

       if (name!=null && !name.isBlank()){
           return projectRepository.findByIdInAndNameContainingIgnoreCase(projectIds,name,pageable);
       }

       if (status != null && !status.isBlank()){
           return projectRepository.findByIdInAndStatus(projectIds, status,pageable);
       }
       return projectRepository.findByIdIn(projectIds, pageable);
    }

    public Project getProjectById(Long projectId){
        UserResponse currentUser= getCurrentLoggedInUser();
        return getProjectById(projectId, currentUser.getId());
    }

    public Project getProjectById(Long projectId, Long userId){
        Project project= findProjectById(projectId);

        boolean isOwner= companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(userId, project.getCompany().getId(), "OWNER", "ACTIVE");
        boolean isActiveMember= companyMembershipRepository.existsByUserIdAndCompanyIdAndStatus(userId, project.getCompany().getId(), "ACTIVE");
        boolean isAssignedManager= projectRepository.existsByIdAndManagersUserIdAndManagersStatus(projectId, userId,"ACTIVE");
        boolean isAssignedWorker= projectRepository.existsByIdAndWorkersUserIdAndWorkersStatus(projectId, userId, "ACTIVE");

         if (!isOwner && (!isActiveMember || (!isAssignedManager && !isAssignedWorker))){
             logger.log(Level.WARNING,"User {0} attempted to view project {1} without permission", new Object[]{userId, projectId});
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to view this project.");
        }
        return project;
    }

    public Project updateProject(Long projectId, ProjectRequest projectRequest){
        UserResponse currentUser= getCurrentLoggedInUser();
        Project project= findProjectById(projectId);

        if (!isCompanyOwner(currentUser, project.getCompany().getId())){
            logger.log(Level.WARNING,"User {0} attempted to update project {1} without permission", new Object[]{currentUser.getId(),projectId});
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to update this project.");
        }

        validateProjectDates(projectRequest);
        setProjectFields(project,projectRequest);

        Project updateProject= projectRepository.save(project);
//        auditLogService.createAuditLog(currentUser,updateProject.getCompany(),"UPDATE","PROJECT",updateProject.getId(),"Updated project: "+updateProject.getName());
        logger.log(Level.INFO, "Project {0} updated successfully", updateProject.getId());
        return updateProject;
    }

    public Project archiveProject(Long projectId){
        UserResponse currentUser= getCurrentLoggedInUser();
        Project project= findProjectById(projectId);

        if (!isCompanyOwner(currentUser, project.getCompany().getId())){
            logger.log(Level.WARNING,"User {0} attempted to archive project {1} without permission", new Object[]{currentUser.getId(), projectId});
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to delete this project.");
        }
        project.setStatus("ARCHIVED");
        Project archivedProject= projectRepository.save(project);
//        auditLogService.createAuditLog(currentUser,archivedProject.getCompany(),"ARCHIVE","PROJECT",archivedProject.getId(),"Archived project: "+archivedProject.getName());
        logger.log(Level.INFO, "Project {0} archived successfully", archivedProject.getId());
        return archivedProject;
    }


    public CompanyMembership assignManager(Long projectId, Long membershipId){
        UserResponse currentUser= getCurrentLoggedInUser();
        Project project= findProjectById(projectId);
        CompanyMembership membership= findMembershipById(membershipId);

        if (!isCompanyOwner(currentUser, project.getCompany().getId())) {
            logger.log(Level.WARNING,"User {0} attempted to assign a manager to project {1} without permission", new Object[]{currentUser.getId(), projectId});
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
//        notificationService.sendNotification(membership.getUser().getId(), "You have been assigned as a manager to project "+project.getName());
//        auditLogService.createAuditLog(currentUser,project.getCompany(),"ASSIGN_MANAGER","PROJECT", project.getId(), "Assigned manager: "+membership.getUser().getName());
        logger.log(Level.INFO, "Manager membership {0} assigned to project {1}", new Object[]{membershipId,projectId});
        return membership;
    }

    public CompanyMembership removeManager(Long projectId, Long membershipId){
        UserResponse currentUser= getCurrentLoggedInUser();
        Project project= findProjectById(projectId);
        CompanyMembership membership= findMembershipById(membershipId);

        if (!isCompanyOwner(currentUser, project.getCompany().getId())) {
            logger.log(Level.WARNING,"User {0} attempted to remove a manager from project {1} without permission.", new Object[]{currentUser.getId(),projectId});
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to remove managers from this project.");
        }

        if (!project.getManagers().contains(membership)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This manager is not assigned to this project.");
        }

        project.getManagers().remove(membership);
        projectRepository.save(project);
//        notificationService.sendNotification(membership.getUser().getId(), "You have been removed as a manager from project "+ project.getName());
//        auditLogService.createAuditLog(currentUser,project.getCompany(),"REMOVE_MANAGER","PROJECT", project.getId(), "Remove manager: "+membership.getUser().getName());
        logger.log(Level.INFO,"Manager membership {0} removed from project {1}", new Object[]{membershipId, projectId});
        return membership;
    }

    public CompanyMembership assignWorker(Long projectId, Long membershipId){
        UserResponse currentUser= getCurrentLoggedInUser();
        Project project= findProjectById(projectId);
        CompanyMembership membership= findMembershipById(membershipId);

        if (!isCompanyOwner(currentUser, project.getCompany().getId())) {
            logger.log(Level.WARNING,"User {0} attempted to assign a worker to project {1} without permission", new Object[]{currentUser.getId(), projectId});
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
//        notificationService.sendNotification(membership.getUser().getId(), "You have been assigned to project "+ project.getName());
//        auditLogService.createAuditLog(currentUser,project.getCompany(),"ASSIGN_WORKER","PROJECT", project.getId(), "Assigned worker: "+membership.getUser().getName());
        logger.log(Level.INFO,"Worker membership {0} assigned project {1}", new Object[]{membershipId,projectId});
        return membership;
    }

    public CompanyMembership removeWorker(Long projectId, Long membershipId){
        UserResponse currentUser= getCurrentLoggedInUser();
        Project project= findProjectById(projectId);
        CompanyMembership membership= findMembershipById(membershipId);

        if (!isCompanyOwner(currentUser, project.getCompany().getId())) {
            logger.log(Level.WARNING,"User {0} attempted to remove a worker from project {1} without permission", new Object[]{currentUser.getId(),projectId});
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to remove workers from this project.");
        }

        if (!project.getWorkers().contains(membership)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This worker is not assigned to this project.");
        }

        project.getWorkers().remove(membership);
        projectRepository.save(project);
//        notificationService.sendNotification(membership.getUser().getId(), "You have been removed from project "+ project.getName());
//        auditLogService.createAuditLog(currentUser,project.getCompany(),"REMOVE_WORKER","PROJECT", project.getId(), "Removed worker: "+membership.getUser().getName());
        logger.log(Level.INFO,"Worker membership {0} removed from project {1}", new Object[]{membershipId, projectId});
        return membership;
    }

    private Project findProjectById(Long projectId){
        return projectRepository.findById(projectId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
    }

    public CompanyMembership findMembershipById(Long membershipId){
        return companyMembershipRepository.findById(membershipId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membership not found."));
    }


    public List<CompanyMembership> getAvailableProjectMembers(Long projectId) {
        UserResponse currentUser = getCurrentLoggedInUser();
        Project project = findProjectById(projectId);

        if (!isCompanyOwner(currentUser, project.getCompany().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You're not allowed to manage project members."
            );
        }

        List<CompanyMembership> companyMembers =
                companyMembershipRepository.findByCompanyIdAndStatus(
                        project.getCompany().getId(), "ACTIVE"
                );

        List<CompanyMembership> availableMembers = new ArrayList<>();
        for (CompanyMembership member : companyMembers) {
            if (member.getRole().equals("MANAGER") &&
                    !project.getManagers().contains(member)) {
                availableMembers.add(member);
            }

            if (member.getRole().equals("WORKER") &&
                    !project.getWorkers().contains(member)) {
                availableMembers.add(member);
            }
        }
        return availableMembers;
    }


    private void validateProjectDates(ProjectRequest projectRequest){
        if (projectRequest.getStartDate() == null || projectRequest.getExpectedEndDate()== null || projectRequest.getExpectedEndDate().isBefore(projectRequest.getStartDate())){
            logger.log(Level.WARNING,"Invalid project dates: start = {0}, expectedEnd={1}", new Object[]{projectRequest.getStartDate(), projectRequest.getExpectedEndDate()});
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

    private boolean isCompanyOwner(UserResponse user, Long companyId){
        return companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(user.getId(), companyId,"OWNER","ACTIVE");
    }

    public Long validateExpenseManagementAccess(Long projectId, Long userId){
        Project project= findProjectById(projectId);

        boolean isCompanyOwner= companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(userId, project.getCompany().getId(), "OWNER","ACTIVE");
        boolean isActiveMember= companyMembershipRepository.existsByUserIdAndCompanyIdAndStatus(userId, project.getCompany().getId(), "ACTIVE");
        boolean isAssignedManager= projectRepository.existsByIdAndManagersUserIdAndManagersStatus(projectId, userId, "ACTIVE");

        if (!isCompanyOwner && !(isAssignedManager && isActiveMember)){
            logger.log(Level.WARNING,
                    "User {0} attempted to manage expenses for project {1} without permission",
                    new Object[]{userId, projectId});

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You're not allowed to manage expenses for this project.");
        }

        CompanyMembership membership= companyMembershipRepository.findByUserIdAndCompanyIdAndStatus(userId, project.getCompany().getId(), "ACTIVE").orElseThrow(
                ()-> new ResponseStatusException(HttpStatus.FORBIDDEN, "Active company membership is not found."));

        return membership.getId();
    }

    public void validateExpenseOwnership(Long projectId, Long membershipId, Long userId){
        Project project= findProjectById(projectId);
        CompanyMembership membership= findMembershipById(membershipId);

        if (!membership.getCompany().getId().equals(project.getCompany().getId()) || !membership.getUserId().equals(userId)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only manage your own expenses.");
        }
    }
}
