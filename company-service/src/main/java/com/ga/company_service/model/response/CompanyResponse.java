package com.ga.company_service.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CompanyResponse {
    private boolean success;
    private String message;
    private Long companyId;
    private String name;
    private String description;
    private String location;
    private String address;
    private String status;
    private Long ownerId;
    private String ownerName;
    private String ownerEmail;

}
