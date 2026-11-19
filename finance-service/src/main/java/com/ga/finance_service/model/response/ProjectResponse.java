package com.ga.finance_service.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponse {
    private Long id;
    private Long companyId;
    private String companyName;
    private String name;
    private String description;
    private String location;
    private LocalDate startDate;
    private LocalDate expectedEndDate;
    private Double budget;
    private String status;
}
