package com.ga.arqemio.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReservationRequest {
    @NotNull(message = "Equipment ID is required.")
    private Long equipmentId;
    @NotNull(message = "Project ID is required.")
    private Long projectId;
    @NotNull(message = "Start date and time are required.")
    private LocalDateTime startDateTime;
    @NotNull(message = "End date and time are required.")
    private LocalDateTime endDateTime;
    private String description;
    @Positive(message = "Quantity must be greater than zero.")
    private int quantity;
}
