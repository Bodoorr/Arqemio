package com.ga.company_service.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationRequest {
    private String name;
    private String email;
    private String password;
    private String profilePicture;
}
