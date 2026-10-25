package com.ga.arqemio.model.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateProfileRequest {
    @Size(min = 8, max = 8, message = "Mobile number must ne exactly 8 digits.")
    private String mobileNumber;
}
