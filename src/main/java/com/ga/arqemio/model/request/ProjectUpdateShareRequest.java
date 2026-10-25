package com.ga.arqemio.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ProjectUpdateShareRequest {
    @NotBlank(message = "Customer email is required.")
    @Email(message = "Please provide a valid customer email address.")
    private String customerEmail;
    @NotBlank(message = "Email subject is required.")
    private String subject;
    @NotBlank(message = "Email message is required.")
    private String message;
}
