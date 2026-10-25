package com.ga.arqemio.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TaskAssignmentRequest {
    @NotNull(message = "Worker membership ID is required.")
    private Long workerMembershipId;
    private LocalDateTime assignedAt;
    private LocalDateTime dueAt;
}
