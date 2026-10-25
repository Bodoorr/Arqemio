package com.ga.arqemio.controller;

import com.ga.arqemio.model.Invitation;
import com.ga.arqemio.model.User;
import com.ga.arqemio.model.request.InvitationRegistrationRequest;
import com.ga.arqemio.model.request.InvitationRequest;
import com.ga.arqemio.model.response.InvitationResponse;
import com.ga.arqemio.model.response.InvitationValidationResponse;
import com.ga.arqemio.service.InvitationService;
import jakarta.validation.Valid;
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
            @Valid @RequestBody InvitationRequest request) {

        Invitation invitation = invitationService.createInvitation(
                request.getEmail(),
                request.getRole(),
                request.getCompanyId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(invitation,"Invitation sent successfully."));
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

    @PostMapping("/register")
    public ResponseEntity<?> registerOwner(@Valid @RequestBody InvitationRegistrationRequest invitationRegistrationRequest){
        User user= invitationService.registerInvitedUser(invitationRegistrationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/accept/{token}")
    public ResponseEntity<?> acceptInvitation(@PathVariable String token){
        Invitation invitation= invitationService.acceptInvitation(token);
        return ResponseEntity.status(HttpStatus.OK).body(toResponse(invitation,"Invitation accepted successfully."));
    }

    @DeleteMapping("/cancel/{invitationId}")
    public ResponseEntity<?> cancelInvitation(@PathVariable Long invitationId){
        Invitation invitation= invitationService.cancelInvitation(invitationId);
        return ResponseEntity.status(HttpStatus.OK).body(toResponse(invitation,"Invitation cancelled successfully."));

    }

    @PutMapping("/resend/{invitationId}")
    public ResponseEntity<?> resendInvitation(@PathVariable Long invitationId){
        Invitation invitation= invitationService.resendInvitation(invitationId);
        return ResponseEntity.status(HttpStatus.OK).body(toResponse(invitation, "Invitation sent again successfully."));
    }

    private InvitationResponse toResponse(Invitation invitation, String message){
        return new InvitationResponse( true,
                message,
                invitation.getId(),
                invitation.getEmail(),
                invitation.getRole(),
                invitation.getStatus(),
                invitation.getUser().getName(),
                invitation.getUser().getId()
        );
    }
}
