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
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class TaskAssignmentService {
    private TaskAssignmentRepository taskAssignmentRepository;
    private TaskRepository taskRepository;
    private CompanyMembershipRepository companyMembershipRepository;
    private ProjectRepository projectRepository;

    public static User getCurrentLoggedInUser() {
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }


    public TaskAssignment assignWorker(Long taskId, Long workerMembershipId, LocalDateTime assignedAt, LocalDateTime dueAt) {
        User currentUser = getCurrentLoggedInUser();
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "task not found."));
        CompanyMembership worker = companyMembershipRepository.findById(workerMembershipId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Worker membership not found."));
        boolean isCompanyOwner = companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(currentUser.getId(), task.getProject().getCompany().getId(), "OWNER", "ACTIVE");
        boolean isAssignedManager = projectRepository.existsByIdAndManagersUserIdAndManagersStatus(task.getProject().getId(), currentUser.getId(), "ACTIVE");

        if (!isCompanyOwner && !isAssignedManager) {
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

        if (dueAt.isBefore(assignedAt)) {
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

        return taskAssignmentRepository.save(taskAssignment);
    }

    public void removeAssignedWorker(Long taskId, Long workerMembershipId) {
        User currentUser = getCurrentLoggedInUser();
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found."));
        boolean isCompanyOwner = companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(currentUser.getId(), task.getProject().getCompany().getId(), "OWNER", "ACTIVE");
        boolean isAssignedManager = projectRepository.existsByIdAndManagersUserIdAndManagersStatus(task.getProject().getId(), currentUser.getId(), "ACTIVE");

        if (!isCompanyOwner && !isAssignedManager) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to remove workers from this task.");
        }

        TaskAssignment taskAssignment = taskAssignmentRepository.findByTaskIdAndAssignedToId(taskId, workerMembershipId).orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "This worker is not assigned to this task."));

        taskAssignmentRepository.delete(taskAssignment);
    }



}