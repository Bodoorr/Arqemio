package com.ga.company_service.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class InvitationValidationResponse {
    private boolean valid;
    private String message;
    private String email;
    private String role;
    private LocalDateTime expiresAt;
}
