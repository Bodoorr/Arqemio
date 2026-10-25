package com.ga.arqemio.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ForgetPasswordRequest {
    @NotBlank(message = "Email is required.")
    @Email(message = "Please provide a valid email address.")
    private String email;
}
