package com.ga.arqemio.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class EquipmentRequest {
    @NotNull(message = "Company ID is required.")
    private Long companyId;
    @NotBlank(message = "Equipment name is required.")
    private String name;
    private String description;
    private String imageUrl;
    @NotBlank(message = "Equipment status is required.")
    private String status;
    @Positive(message = "Quantity must be greater than zero.")
    private int quantity;
}
