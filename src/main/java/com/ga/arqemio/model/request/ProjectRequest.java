package com.ga.arqemio.model.request;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ProjectRequest {
    Long companyId;
    String name;
    String description;
    String location;
    LocalDate startDate;
    LocalDate expectedEndDate;
    Double budget;
    String status;
}
