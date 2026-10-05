package com.ga.arqemio.service;

import com.ga.arqemio.model.Project;
import com.ga.arqemio.model.Task;
import com.ga.arqemio.model.User;
import com.ga.arqemio.model.request.ProjectRequest;
import com.ga.arqemio.model.request.TaskRequest;
import com.ga.arqemio.repository.CompanyMembershipRepository;
import com.ga.arqemio.repository.ProjectRepository;
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

    public static User getCurrentLoggedInUser(){
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    public Task createTask(TaskRequest taskRequest){
        User currentUser= getCurrentLoggedInUser();
        Project project= projectRepository.findById(taskRequest.getProjectId()).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
        boolean isPlatformAdmin = currentUser.getIsPlatformAdmin().equals(true);
        boolean isCompanyOwner = companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(
                        currentUser.getId(),
                        project.getCompany().getId(),
                        "OWNER",
                        "ACTIVE"
                );
        if (!isPlatformAdmin && !isCompanyOwner){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to create Tasks for this project.");
        }

        Task task=new Task();
        task.setProject(project);
        task.setTitle(taskRequest.getTitle());
        task.setDescription(taskRequest.getDescription());
        task.setStatus(taskRequest.getStatus());
        task.setPriority(taskRequest.getPriority());
        task.setDueDateTime(taskRequest.getDueDateTime());

        return taskRepository.save(task);
    }

    public List<Task> getAllTasks(){
        User currentUser= getCurrentLoggedInUser();

        if (currentUser.getIsPlatformAdmin()){
            return taskRepository.findAll();
        }

        return taskRepository.findByProjectCompanyMembershipsUserIdAndProjectCompanyMembershipsStatus(
                currentUser.getId(),
                "ACTIVE"
        );
    }

    public Task getTaskById(Long taskId){
        User currentUser= getCurrentLoggedInUser();

        Task task= taskRepository.findById(taskId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "task not found."));
        boolean isPlatformAdmin= currentUser.getIsPlatformAdmin();
        boolean isActiveMember= companyMembershipRepository.existsByUserIdAndCompanyIdAndStatus(currentUser.getId(), task.getProject().getCompany().getId(), "ACTIVE");
        if (!isActiveMember && !isPlatformAdmin){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to view this task.");
        }

        return task;
    }

    public Task updateTask(Long taskId, TaskRequest taskRequest){
        User currentUser= getCurrentLoggedInUser();

        Task task= taskRepository.findById(taskId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "task not found."));
        boolean isPlatformAdmin= currentUser.getIsPlatformAdmin();
        boolean isCompanyOwner= companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(currentUser.getId(), task.getProject().getCompany().getId(), "OWNER", "ACTIVE");
        if (!isPlatformAdmin && !isCompanyOwner){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to update this task.");
        }

        if (taskRequest.getDueDateTime().isBefore(task.getCreatedAt())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task due date cannot be before the task creation date.");
        }

        task.setTitle(taskRequest.getTitle());
        task.setDescription(taskRequest.getDescription());
        task.setStatus(taskRequest.getStatus());
        task.setPriority(taskRequest.getPriority());
        task.setDueDateTime(taskRequest.getDueDateTime());

        return taskRepository.save(task);

    }


}
