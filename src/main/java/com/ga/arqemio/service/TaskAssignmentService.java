package com.ga.arqemio.service;

import com.ga.arqemio.model.CompanyMembership;
import com.ga.arqemio.model.Task;
import com.ga.arqemio.model.TaskAssignment;
import com.ga.arqemio.model.User;
import com.ga.arqemio.repository.CompanyMembershipRepository;
import com.ga.arqemio.repository.ProjectRepository;
import com.ga.arqemio.repository.TaskAssignmentRepository;
import com.ga.arqemio.repository.TaskRepository;
import com.ga.arqemio.security.MyUserDetails;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@AllArgsConstructor
public class TaskAssignmentService {
    private TaskAssignmentRepository taskAssignmentRepository;
    private TaskRepository taskRepository;
    private CompanyMembershipRepository companyMembershipRepository;
    private ProjectRepository projectRepository;
    private static final Logger logger= Logger.getLogger(TaskAssignmentService.class.getName());
    private NotificationService notificationService;
    private static final ExecutorService executorService= Executors.newFixedThreadPool(3);

    public static User getCurrentLoggedInUser() {
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    public TaskAssignment assignWorker(Long taskId, Long workerMembershipId, LocalDateTime assignedAt, LocalDateTime dueAt) {
        User currentUser = getCurrentLoggedInUser();
        Task task= findTaskById(taskId);
        CompanyMembership worker = companyMembershipRepository.findById(workerMembershipId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Worker membership not found."));

        if (!canManageTaskAssignments(currentUser, task)) {
            logger.log(Level.WARNING, "User {0} attempted to assign a worker to task {1} without permission", new Object[]{currentUser.getId(), taskId});
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to assign workers to this task.");
        }

        if (!worker.getCompany().getId().equals(task.getProject().getCompany().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This worker does not belong to the project's company.");
        }

        if (!worker.getRole().equals("WORKER")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This member is not a worker.");
        }

        if (!worker.getStatus().equals("ACTIVE")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This worker is not active.");
        }


        boolean isProjectWorker = projectRepository.existsByIdAndWorkersId(task.getProject().getId(), workerMembershipId);

        if (!isProjectWorker) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This worker is not assigned to this project.");
        }

        boolean isAlreadyAssigned = taskAssignmentRepository.existsByTaskIdAndAssignedToId(taskId, workerMembershipId);

        if (isAlreadyAssigned) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This worker is already assigned to this task.");
        }

        if (assignedAt != null && dueAt != null && dueAt.isBefore(assignedAt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Due date cannot be before assigned date.");
        }

        CompanyMembership assignedBy = companyMembershipRepository.findByUserIdAndCompanyIdAndStatus(currentUser.getId(), task.getProject().getCompany().getId(), "ACTIVE")
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "Active company membership not found."
                        ));
        TaskAssignment taskAssignment = new TaskAssignment();
        taskAssignment.setTask(task);
        taskAssignment.setAssignedBy(assignedBy);
        taskAssignment.setAssignedTo(worker);
        taskAssignment.setAssignedAt(assignedAt);
        taskAssignment.setDueAt(dueAt);

        TaskAssignment savedAssignment= taskAssignmentRepository.save(taskAssignment);
        notifyWorker(worker.getUser().getId(), savedAssignment.getTask().getId());
        logger.log(Level.INFO,"Worker membership {0} assigned to task {1} by user {2}", new Object[]{workerMembershipId, taskId, currentUser.getId()});
        return savedAssignment;
    }

    public TaskAssignment removeAssignedWorker(Long taskId, Long workerMembershipId) {
        User currentUser = getCurrentLoggedInUser();
        Task task = findTaskById(taskId);

        if (!canManageTaskAssignments(currentUser, task)) {
            logger.log(Level.WARNING,"User {0} attempted to remove worker from task {1} without permission", new Object[]{currentUser.getId(), taskId});
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to remove workers from this task.");
        }

        TaskAssignment taskAssignment = taskAssignmentRepository.findByTaskIdAndAssignedToId(taskId, workerMembershipId).orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "This worker is not assigned to this task."));

        taskAssignmentRepository.delete(taskAssignment);
        logger.log(Level.INFO,"Worker membership {0} removed from task {1} by user {2}", new Object[]{workerMembershipId, taskId, currentUser.getId()});
        return taskAssignment;
    }

    private boolean canManageTaskAssignments(User user, Task task){
        boolean isCompanyOwner = companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(user.getId(), task.getProject().getCompany().getId(), "OWNER", "ACTIVE");
        boolean isAssignedManager = projectRepository.existsByIdAndManagersUserIdAndManagersStatus(task.getProject().getId(), user.getId(), "ACTIVE");

        return isCompanyOwner || isAssignedManager;
    }

    private Task findTaskById(Long taskId){
        return taskRepository.findById(taskId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found."));
    }

    private void notifyWorker(Long userId, Long taskId){
        executorService.execute(()->{
            logger.info("Thread: "+Thread.currentThread().getName());
            notificationService.sendNotification(userId,"You have been assigned to task "+taskId);
        });
    }

    @PreDestroy
    public void shutdown(){
        executorService.shutdown();
    }
}