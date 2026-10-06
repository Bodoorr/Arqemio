package com.ga.arqemio.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReservationResponse {
    private Long id;
    private Long equipmentId;
    private String equipmentName;
    private Long projectId;
    private String projectName;
    private Long reservedByMembershipId;
    private String reservedByName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String description;
    private String status;
    private int quantity;
}
