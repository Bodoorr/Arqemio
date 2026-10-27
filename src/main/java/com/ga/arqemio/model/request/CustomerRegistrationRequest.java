package com.ga.arqemio.model.request;

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
    @Size(min=8)
    String password;

    @NotBlank
    @Size(min = 8)
    String confirmedPassword;
}
