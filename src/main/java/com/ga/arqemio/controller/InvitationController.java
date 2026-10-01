package com.ga.arqemio.controller;

import com.ga.arqemio.model.Invitation;
import com.ga.arqemio.model.request.InvitationRequest;
import com.ga.arqemio.model.response.InvitationResponse;
import com.ga.arqemio.model.response.InvitationValidationResponse;
import com.ga.arqemio.service.InvitationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/validate/{token}")
    public ResponseEntity<InvitationValidationResponse> validateInvitation(@PathVariable String token){
        Invitation invitation= invitationService.validateInvitation(token);

        InvitationValidationResponse response=new InvitationValidationResponse(
                true,
                "Invitation is valid.",
                invitation.getEmail(),
                invitation.getRole(),
                invitation.getExpiresAt()
        );
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


}
