package com.ga.company_service.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CompanyRequest {
    @NotBlank(message = "Company name is required.")
    private String name;
    private String description;
    private String location;
    private String address;
}
