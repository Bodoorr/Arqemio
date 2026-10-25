package com.ga.arqemio.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ProjectRequest {
    @NotNull(message = "Company ID is required.")
    Long companyId;
    @NotBlank(message = "Project name is required.")
    String name;
    String description;
    String location;
    @NotNull(message = "Start date is required.")
    LocalDate startDate;
    @NotNull(message = "Expected end date is required.")
    LocalDate expectedEndDate;
    @Positive(message = "Budget must be greater than zero.")
    Double budget;
    @NotBlank(message = "Project status is required.")
    String status;
}
