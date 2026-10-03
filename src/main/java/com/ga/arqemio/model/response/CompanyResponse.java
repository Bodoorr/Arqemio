package com.ga.arqemio.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CompanyResponse {
    private boolean success;
    private boolean message;
    private Long companyId;
    private String name;
    private String description;
    private String location;
    private String address;
    private String status;

}
