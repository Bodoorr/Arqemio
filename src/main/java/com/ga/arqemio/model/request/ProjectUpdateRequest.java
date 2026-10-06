package com.ga.arqemio.model.request;

import lombok.Getter;

@Getter
public class ProjectUpdateRequest {
    private Long projectId;
    private String title;
    private String description;
}
