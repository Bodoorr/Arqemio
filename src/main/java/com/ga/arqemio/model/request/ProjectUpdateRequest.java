package com.ga.arqemio.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ProjectUpdateRequest {
    @NotNull(message = "Project ID is required.")
    private Long projectId;
    @NotBlank(message = "Update title is required.")
    private String title;
    @NotBlank(message = "Update description is required.")
    private String description;
}
