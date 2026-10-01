package com.ga.arqemio.model.response;

import com.ga.arqemio.model.Invitation;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InvitationResponse {
    private boolean success;
    private String message;
    private Long invitationId;
    private String email;
    private String role;
    private String status;
    private String invitedBy;
    private Long invitedById;
}
