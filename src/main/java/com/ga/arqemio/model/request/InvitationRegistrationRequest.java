package com.ga.arqemio.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class InvitationRegistrationRequest {
    @NotBlank(message = "Invitation token is required.")
    private String token;
    @NotBlank(message = "Name is required.")
    private String name;
    private String profilePicture;
    @NotBlank(message = "Password is required.")
    @Size(min = 8, message = "Password must be at least 8 characters.")
    private String password;
}
