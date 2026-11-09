package com.ga.company_service.controller;

import com.ga.company_service.model.TaskAssignment;
import com.ga.company_service.model.request.TaskAssignmentRequest;
import com.ga.company_service.model.response.TaskAssignmentResponse;
import com.ga.company_service.service.TaskAssignmentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/company/projects/tasks")
@AllArgsConstructor
public class TaskAssignmentController {
    private TaskAssignmentService taskAssignmentService;

    @PostMapping("/{taskId}/assignments")
    public ResponseEntity<TaskAssignmentResponse> assignWorker(@PathVariable Long taskId, @Valid @RequestBody TaskAssignmentRequest taskAssignmentRequest) {
        TaskAssignment taskAssignment = taskAssignmentService.assignWorker(
                taskId,
                taskAssignmentRequest.getWorkerMembershipId(),
                taskAssignmentRequest.getAssignedAt(),
                taskAssignmentRequest.getDueAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(taskAssignment));
    }

    @DeleteMapping("/{taskId}/assignments/{workerMembershipId}")
    public ResponseEntity<TaskAssignmentResponse> removeAssignedWorker(@PathVariable Long taskId, @PathVariable Long workerMembershipId){
        TaskAssignment taskAssignment= taskAssignmentService.removeAssignedWorker(taskId, workerMembershipId);
        return ResponseEntity.ok(toResponse(taskAssignment));
    }

    private TaskAssignmentResponse toResponse(TaskAssignment taskAssignment){
        return new TaskAssignmentResponse(
                taskAssignment.getId(),
                taskAssignment.getTask().getId(),
                taskAssignment.getTask().getTitle(),
                taskAssignment.getAssignedTo().getId(),
                taskAssignment.getAssignedTo().getUser().getName(),
                taskAssignment.getAssignedBy().getId(),
                taskAssignment.getAssignedBy().getUser().getName(),
                taskAssignment.getAssignedAt(),
                taskAssignment.getDueAt()
        );
    }
}
