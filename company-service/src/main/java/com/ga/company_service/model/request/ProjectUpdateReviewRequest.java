package com.ga.company_service.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ProjectUpdateReviewRequest {
    @NotBlank(message = "Review status is required.")
    private String status;
    private String reviewNote;
}
