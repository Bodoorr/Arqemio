package com.ga.arqemio.model.request;

import lombok.Getter;

@Getter
public class InvitationRegistrationRequest {
    private String token;
    private String name;
    private String password;
}
