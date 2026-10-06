package com.ga.arqemio.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class EquipmentResponse {
    private Long id;
    private Long companyId;
    private String companyName;
    private String name;
    private String description;
    private String imageUrl;
    private String status;
    private int quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
