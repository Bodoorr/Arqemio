package com.ga.arqemio.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class InvitationRequest {
    @NotBlank(message = "Email is required.")
    @Email(message = "Please provide a valid email address.")
    private String email;
    @NotBlank(message = "Role is required.")
    private String role;
    @NotNull(message = "Company ID is required.")
    private Long companyId;
}
