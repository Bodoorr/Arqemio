package com.ga.arqemio.model.request;

import lombok.Getter;

@Getter
public class ProjectUpdateShareRequest {
    private String customerEmail;
    private String subject;
    private String message;
}
