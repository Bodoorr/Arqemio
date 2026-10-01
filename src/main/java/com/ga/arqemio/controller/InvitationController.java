package com.ga.arqemio.controller;

import com.ga.arqemio.model.Invitation;
import com.ga.arqemio.model.request.InvitationRequest;
import com.ga.arqemio.model.response.InvitationResponse;
import com.ga.arqemio.service.InvitationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/invitation")
@AllArgsConstructor
public class InvitationController {
    private InvitationService invitationService;

    @PostMapping
    public ResponseEntity<InvitationResponse> createInvitation(
            @RequestBody InvitationRequest request) {

        Invitation invitation = invitationService.createInvitation(
                request.getEmail(),
                request.getRole()
        );

        InvitationResponse response=new InvitationResponse(
                true,
                "Invitation sent successfully.",
                invitation.getId(),
                invitation.getEmail(),
                invitation.getRole(),
                invitation.getStatus(),
                invitation.getUser().getName(),
                invitation.getUser().getId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


}
