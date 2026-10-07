package com.ga.arqemio.model.request;

import lombok.Getter;

@Getter
public class ResetPasswordRequest {
    private String token;
    private String newPassword;
    private String confirmPassword;
}
