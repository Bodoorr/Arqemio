package com.ga.arqemio.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TaskStatusRequest {
    @NotBlank(message = "Task status is required.")
    private String status;
}
