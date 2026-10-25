package com.ga.arqemio.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ProjectUpdateReviewRequest {
    @NotBlank(message = "Review status is required.")
    private String status;
    private String reviewNote;
}
