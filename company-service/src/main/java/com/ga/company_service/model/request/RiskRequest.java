package com.ga.company_service.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RiskRequest {
    private Long projectId;
    private String title;
    private String location;
    private String category;
}
