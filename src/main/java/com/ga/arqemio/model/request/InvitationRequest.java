package com.ga.arqemio.model.request;

import lombok.Getter;

@Getter
public class InvitationRequest {
    private String email;
    private String role;
    private Long companyID;
}
