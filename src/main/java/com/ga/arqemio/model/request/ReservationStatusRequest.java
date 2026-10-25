package com.ga.arqemio.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ReservationStatusRequest {
    @NotBlank(message = "Reservation status is required.")
    private String status;
}
