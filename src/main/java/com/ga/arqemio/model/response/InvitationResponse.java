package com.ga.arqemio.model.response;

import com.ga.arqemio.model.Invitation;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InvitationResponse {
    private boolean success;
    private String message;
    private Invitation invitation;
}
