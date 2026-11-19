package com.ga.company_service.service;

import com.ga.company_service.client.AuditLogClient;
import com.ga.company_service.model.Project;
import com.ga.company_service.model.Task;
import com.ga.company_service.model.request.TaskRequest;
import com.ga.company_service.model.response.UserResponse;
import com.ga.company_service.repository.CompanyMembershipRepository;
import com.ga.company_service.repository.ProjectRepository;
import com.ga.company_service.repository.TaskAssignmentRepository;
import com.ga.company_service.repository.TaskRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@AllArgsConstructor
public class TaskService {
    private TaskRepository taskRepository;
    private ProjectRepository projectRepository;
    private ProjectService projectService;
    private CompanyMembershipRepository companyMembershipRepository;
    private TaskAssignmentRepository taskAssignmentRepository;
    private AuditLogClient auditLogClient;
    private static final Logger logger = Logger.getLogger(TaskService.class.getName());

    public static UserResponse getCurrentLoggedInUser(){
        return (UserResponse) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public Task createTask(TaskRequest taskRequest){
        UserResponse currentUser= getCurrentLoggedInUser();
        Project project= projectRepository.findById(taskRequest.getProjectId()).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));

        if (!canManageTask(currentUser,project)){
            logger.log(Level.WARNING, "User {0} attempted to create a task in project {1} without permission", new Object[]{currentUser.getId(), project.getId()});
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to create Tasks for this project.");
        }

        Task task=new Task();
        task.setProject(project);
        setTaskFields(task, taskRequest);
        task.setStatus("TODO");

        Task savedTask= taskRepository.save(task);
        auditLogClient.createAuditLog(currentUser.getId(), project.getCompany().getId(), "CREATE", "TASK", savedTask.getId(), "Created task: " + savedTask.getTitle());
        logger.log(Level.INFO, "Task {0} created in project {1} by user {2}", new Object[]{savedTask.getId(), project.getId(), currentUser.getId()});

        return savedTask;
    }

    public List<Task> getAllTasks(){
      List<Project> projects= projectService.getAllProjects();
      List<Task> tasks= new ArrayList<>();

      for (Project project: projects){
          List<Task> projectTasks= taskRepository.findByProjectId(project.getId());

          for (Task task: projectTasks){
              tasks.add(task);
          }
      }
      return tasks;
    }

    public Task getTaskById(Long taskId){
        Task task= findTaskById(taskId);
        projectService.getProjectById(task.getProject().getId());
        return task;
    }

    public Task updateTask(Long taskId, TaskRequest taskRequest){
        UserResponse currentUser= getCurrentLoggedInUser();
        Task task= findTaskById(taskId);

        if (!canManageTask(currentUser, task.getProject())){
            logger.log(Level.WARNING,"User {0} attempted to update task {1} without permission", new Object[]{currentUser.getId(), taskId});
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to update this task.");
        }

        if (taskRequest.getDueDateTime().isBefore(task.getCreatedAt())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task due date cannot be before the task creation date.");
        }

        setTaskFields(task,taskRequest);

        Task savedTask= taskRepository.save(task);
        auditLogClient.createAuditLog(currentUser.getId(), savedTask.getProject().getCompany().getId(), "UPDATE", "TASK", savedTask.getId(), "Updated task: " + savedTask.getTitle());
        logger.log(Level.INFO,"Task {0} updated by user {1}", new Object[]{savedTask.getId(), currentUser.getId()});
        return savedTask;

    }

    public Task archiveTask(Long taskId){
        UserResponse currentUser= getCurrentLoggedInUser();
        Task task= findTaskById(taskId);

        if (!canManageTask(currentUser, task.getProject())){
            logger.log(Level.WARNING, "User {0} attempted to archive task {1} without permission", new Object[]{currentUser.getId(), taskId});
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to delete this task.");
        }

        task.setStatus("ARCHIVED");

        Task savedTask= taskRepository.save(task);
        auditLogClient.createAuditLog(currentUser.getId(), savedTask.getProject().getCompany().getId(), "ARCHIVE", "TASK", savedTask.getId(), "Archived task: " + savedTask.getTitle());
        logger.log(Level.INFO,"Task {0} archived by user {1}", new Object[]{savedTask.getId(), currentUser.getId()});
        return savedTask;
    }

    public Task updateTaskStatus(Long taskId, String status){
        UserResponse currentUser= getCurrentLoggedInUser();
        Task task= findTaskById(taskId);

        boolean isAssignedWorker= taskAssignmentRepository.existsByTaskIdAndAssignedToUserIdAndAssignedToStatus(taskId,currentUser.getId(),"ACTIVE");
        boolean isActiveWorker= companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(currentUser.getId(), task.getProject().getCompany().getId(), "WORKER", "ACTIVE");

        if (!isAssignedWorker || !isActiveWorker){
            logger.log(Level.WARNING,"User {0} attempted to change status of task {1} without assignment", new Object[]{currentUser.getId(), taskId});
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not assigned to this task.");
        }

        task.setStatus(status);
        Task savedTask= taskRepository.save(task);

        auditLogClient.createAuditLog(currentUser.getId(), savedTask.getProject().getCompany().getId(), "UPDATE_STATUS", "TASK", savedTask.getId(), "Updated task status to " + status);
        logger.log(Level.INFO,"Task {0} status updated to {1} by user {2}", new Object[]{savedTask.getId(), status, currentUser.getId()});
        return savedTask;
    }


    private boolean canManageTask(UserResponse user, Project project){
        boolean isCompanyOwner= companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(user.getId(), project.getCompany().getId(), "OWNER", "ACTIVE");
        boolean isActiveMember= companyMembershipRepository.existsByUserIdAndCompanyIdAndStatus(user.getId(), project.getCompany().getId(), "ACTIVE");
        boolean isAssignedManager= projectRepository.existsByIdAndManagersUserIdAndManagersStatus(project.getId(), user.getId(), "ACTIVE");
        return isCompanyOwner || (isActiveMember && isAssignedManager);
    }

    private Task findTaskById(Long taskId){
        return taskRepository.findById(taskId)
                .orElseThrow(()->new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task not found."));
    }

    private void setTaskFields(Task task, TaskRequest taskRequest){
        task.setTitle(taskRequest.getTitle());
        task.setDescription(taskRequest.getDescription());
        task.setPriority(taskRequest.getPriority());
        task.setDueDateTime(taskRequest.getDueDateTime());
    }
}
