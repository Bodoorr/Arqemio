package com.ga.arqemio.model.request;

import lombok.Getter;

@Getter
public class EquipmentRequest {
    private Long companyId;
    private String name;
    private String description;
    private String imageUrl;
    private String status;
    private int quantity;
}
