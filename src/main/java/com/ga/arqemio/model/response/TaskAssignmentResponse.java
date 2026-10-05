package com.ga.arqemio.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TaskAssignmentResponse {
    private Long id;
    private Long taskId;
    private String taskTitle;
    private Long workerMembershipId;
    private String workerName;
    private Long assignedByMembershipId;
    private String assignedByName;
    private LocalDateTime assignedAt;
    private LocalDateTime dueAt;
}
