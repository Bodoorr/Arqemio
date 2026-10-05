package com.ga.arqemio.model.request;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TaskRequest {
    private Long projectId;
    private String title;
    private String description;
    private String status;
    private String priority;
    private LocalDateTime dueDateTime;
}
