package com.ga.arqemio.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ProjectResponse {
    private boolean success;
    private String message;
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
