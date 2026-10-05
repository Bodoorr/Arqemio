package com.ga.arqemio.model.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TaskResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private String title;
    private String description;
    private String status;
    private String priority;
    private LocalDateTime dueDateTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
