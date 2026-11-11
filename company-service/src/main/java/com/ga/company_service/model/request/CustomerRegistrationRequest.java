package com.ga.company_service.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRegistrationRequest {
    @NotBlank
    String name;

    @NotBlank
    @Email
    String email;

    @NotBlank
    @Size(min=8)
    String password;

    @NotBlank
    @Size(min = 8)
    String confirmedPassword;
}
