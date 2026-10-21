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

@Service
@AllArgsConstructor
public class TaskService {
    private TaskRepository taskRepository;
    private ProjectRepository projectRepository;
    private CompanyMembershipRepository companyMembershipRepository;
    private TaskAssignmentRepository taskAssignmentRepository;

    public static User getCurrentLoggedInUser(){
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    public Task createTask(TaskRequest taskRequest){
        User currentUser= getCurrentLoggedInUser();
        Project project= projectRepository.findById(taskRequest.getProjectId()).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));

        if (!canManageTask(currentUser,project)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to create Tasks for this project.");
        }

        Task task=new Task();
        task.setProject(project);
        setTaskFields(task, taskRequest);

        return taskRepository.save(task);
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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to view this task.");
        }

        return task;
    }

    public Task updateTask(Long taskId, TaskRequest taskRequest){
        User currentUser= getCurrentLoggedInUser();
        Task task= findTaskById(taskId);

        if (!canManageTask(currentUser, task.getProject())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to update this task.");
        }

        if (taskRequest.getDueDateTime().isBefore(task.getCreatedAt())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task due date cannot be before the task creation date.");
        }

        setTaskFields(task,taskRequest);

        return taskRepository.save(task);

    }

    public Task archiveTask(Long taskId){
        User currentUser= getCurrentLoggedInUser();
        Task task= findTaskById(taskId);

        if (!canManageTask(currentUser, task.getProject())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to delete this task.");
        }

        task.setStatus("ARCHIVED");

        return taskRepository.save(task);
    }

    public Task updateTaskStatus(Long taskId, String status){
        User currentUser= getCurrentLoggedInUser();
        Task task= findTaskById(taskId);
        boolean isAssignedWorker= taskAssignmentRepository.existsByTaskIdAndAssignedToUserIdAndAssignedToStatus(taskId,currentUser.getId(),"ACTIVE");

        if (!isAssignedWorker){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not assigned to this task.");
        }

        task.setStatus(status);
        return taskRepository.save(task);
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
