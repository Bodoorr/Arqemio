package com.ga.arqemio.service;

import com.ga.arqemio.model.Project;
import com.ga.arqemio.model.Task;
import com.ga.arqemio.model.User;
import com.ga.arqemio.model.request.TaskRequest;
import com.ga.arqemio.repository.CompanyMembershipRepository;
import com.ga.arqemio.repository.ProjectRepository;
import com.ga.arqemio.repository.TaskAssignmentRepository;
import com.ga.arqemio.repository.TaskRepository;
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
public class TaskService {
    private TaskRepository taskRepository;
    private ProjectRepository projectRepository;
    private CompanyMembershipRepository companyMembershipRepository;
    private TaskAssignmentRepository taskAssignmentRepository;
    private static final Logger logger = Logger.getLogger(TaskService.class.getName());

    public static User getCurrentLoggedInUser(){
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    public Task createTask(TaskRequest taskRequest){
        User currentUser= getCurrentLoggedInUser();
        Project project= projectRepository.findById(taskRequest.getProjectId()).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));

        if (!canManageTask(currentUser,project)){
            logger.log(Level.WARNING, "User {0} attempted to create a task in project {1} without permission", new Object[]{currentUser.getId(), project.getId()});
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to create Tasks for this project.");
        }

        Task task=new Task();
        task.setProject(project);
        setTaskFields(task, taskRequest);

        Task savedTask= taskRepository.save(task);
        logger.log(Level.INFO, "Task {0} created in project {1} by user {2}", new Object[]{savedTask.getId(), project.getId(), currentUser.getId()});

        return savedTask;
    }

    public List<Task> getAllTasks(){
        User currentUser= getCurrentLoggedInUser();
        return taskRepository.findByProjectCompanyMembershipsUserIdAndProjectCompanyMembershipsStatus(
                currentUser.getId(),
                "ACTIVE"
        );
    }

    public Task getTaskById(Long taskId){
        User currentUser= getCurrentLoggedInUser();
        Task task= findTaskById(taskId);

        boolean isActiveMember= companyMembershipRepository.existsByUserIdAndCompanyIdAndStatus(currentUser.getId(), task.getProject().getCompany().getId(), "ACTIVE");
        if (!isActiveMember){
            logger.log(Level.WARNING,"User {0} attempted to view task {1} without permission", new Object[]{currentUser.getId(), taskId});
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to view this task.");
        }

        return task;
    }

    public Task updateTask(Long taskId, TaskRequest taskRequest){
        User currentUser= getCurrentLoggedInUser();
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
        logger.log(Level.INFO,"Task {0} updated by user {1}", new Object[]{savedTask.getId(), currentUser.getId()});
        return savedTask;

    }

    public Task archiveTask(Long taskId){
        User currentUser= getCurrentLoggedInUser();
        Task task= findTaskById(taskId);

        if (!canManageTask(currentUser, task.getProject())){
            logger.log(Level.WARNING, "User {0} attempted to archive task {1} without permission", new Object[]{currentUser.getId(), taskId});
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to delete this task.");
        }

        task.setStatus("ARCHIVED");

        Task savedTask= taskRepository.save(task);
        logger.log(Level.INFO,"Task {0} archived by user {1}", new Object[]{savedTask.getId(), currentUser.getId()});
        return savedTask;
    }

    public Task updateTaskStatus(Long taskId, String status){
        User currentUser= getCurrentLoggedInUser();
        Task task= findTaskById(taskId);
        boolean isAssignedWorker= taskAssignmentRepository.existsByTaskIdAndAssignedToUserIdAndAssignedToStatus(taskId,currentUser.getId(),"ACTIVE");

        if (!isAssignedWorker){
            logger.log(Level.WARNING,"User {0} attempted to change status of task {1} without assignment", new Object[]{currentUser.getId(), taskId});
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not assigned to this task.");
        }

        task.setStatus(status);
        Task savedTask= taskRepository.save(task);

        logger.log(Level.INFO,"Task {0} status updated to {1} by user {2}", new Object[]{savedTask.getId(), status, currentUser.getId()});
        return savedTask;
    }


    private boolean canManageTask(User user, Project project){
        boolean isCompanyOwner= companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(user.getId(), project.getCompany().getId(), "OWNER", "ACTIVE");
        boolean isAssignedManager= projectRepository.existsByIdAndManagersUserIdAndManagersStatus(project.getId(), user.getId(), "ACTIVE");
        return isCompanyOwner || isAssignedManager;
    }

    private Task findTaskById(Long taskId){
        return taskRepository.findById(taskId)
                .orElseThrow(()->new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task not found."));

    }

    private void setTaskFields(Task task, TaskRequest taskRequest){
        task.setTitle(taskRequest.getTitle());
        task.setDescription(taskRequest.getDescription());
        task.setStatus(taskRequest.getStatus());
        task.setPriority(taskRequest.getPriority());
        task.setDueDateTime(taskRequest.getDueDateTime());
    }
}
