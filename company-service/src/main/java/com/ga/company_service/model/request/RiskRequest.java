package com.ga.company_service.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RiskRequest {
    @NotNull(message = "Project ID is required.")
    private Long projectId;

    @NotBlank(message = "Title is required.")
    private String title;

    @NotBlank(message = "Location is required.")
    private String location;

    @NotBlank(message = "Category is required.")
    private String category;
}
