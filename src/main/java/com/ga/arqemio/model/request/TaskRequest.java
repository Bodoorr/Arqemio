package com.ga.arqemio.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TaskRequest {
    @NotNull(message = "Project ID is required.")
    private Long projectId;
    @NotBlank(message = "Task title is required.")
    private String title;
    private String description;
    @NotBlank(message = "Task status is required.")
    private String status;
    @NotBlank(message = "Task priority is required.")
    private String priority;
    private LocalDateTime dueDateTime;
}
