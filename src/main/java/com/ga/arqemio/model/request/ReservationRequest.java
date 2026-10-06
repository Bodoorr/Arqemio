package com.ga.arqemio.model.request;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReservationRequest {
    private Long equipmentId;
    private Long projectId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String description;
    private int quantity;
}
