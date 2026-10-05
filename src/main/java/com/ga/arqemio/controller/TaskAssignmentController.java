package com.ga.arqemio.controller;

import com.ga.arqemio.model.TaskAssignment;
import com.ga.arqemio.model.request.TaskAssignmentRequest;
import com.ga.arqemio.model.response.TaskAssignmentResponse;
import com.ga.arqemio.service.TaskAssignmentService;
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
    public ResponseEntity<TaskAssignmentResponse> assignWorker(@PathVariable Long taskId, @RequestBody TaskAssignmentRequest taskAssignmentRequest) {
        TaskAssignment taskAssignment = taskAssignmentService.assignWorker(
                taskId,
                taskAssignmentRequest.getWorkerMembershipId(),
                taskAssignmentRequest.getAssignedAt(),
                taskAssignmentRequest.getDueAt()
        );
        TaskAssignmentResponse taskAssignmentResponse = new TaskAssignmentResponse(
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
        return ResponseEntity.status(HttpStatus.CREATED).body(taskAssignmentResponse);
    }

    @DeleteMapping("/{taskId}/assignments/{workerMembershipId}")
    public ResponseEntity<TaskAssignmentResponse> removeAssignedWorker(@PathVariable Long taskId, @PathVariable Long workerMembershipId){
        TaskAssignment taskAssignment= taskAssignmentService.removeAssignedWorker(taskId, workerMembershipId);

        TaskAssignmentResponse taskAssignmentResponse = new TaskAssignmentResponse(
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
        return ResponseEntity.ok(taskAssignmentResponse);
    }
}
