package com.ga.arqemio.model.request;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TaskAssignmentRequest {
    private Long workerMembershipId;
    private LocalDateTime assignedAt;
    private LocalDateTime dueAt;
}
