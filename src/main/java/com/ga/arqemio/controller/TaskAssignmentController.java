package com.ga.arqemio.controller;

import com.ga.arqemio.model.TaskAssignment;
import com.ga.arqemio.model.request.TaskAssignmentRequest;
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
    public ResponseEntity<TaskAssignment> assignWorker(@PathVariable Long taskId, @RequestBody TaskAssignmentRequest taskAssignmentRequest){
        TaskAssignment taskAssignment= taskAssignmentService.assignWorker(
                taskId,
                taskAssignmentRequest.getWorkerMembershipId(),
                taskAssignmentRequest.getAssignedAt(),
                taskAssignmentRequest.getDueAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(taskAssignment);
    }


}
