package com.ga.auth_service.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserResponse {
    private Long id;
    private String email;
    private Boolean isPlatformAdmin;
    private Boolean isCustomer;
}
