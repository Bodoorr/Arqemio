package com.ga.company_service.controller;

import com.ga.company_service.client.AuthClient;
import com.ga.company_service.model.Invitation;
import com.ga.company_service.model.request.InvitationRegistrationRequest;
import com.ga.company_service.model.request.InvitationRequest;
import com.ga.company_service.model.response.InvitationResponse;
import com.ga.company_service.model.response.InvitationValidationResponse;
import com.ga.company_service.model.response.UserResponse;
import com.ga.company_service.service.InvitationService;
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
    private AuthClient authClient;

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
    public ResponseEntity<UserResponse> registerOwner(@Valid @RequestBody InvitationRegistrationRequest invitationRegistrationRequest){
        UserResponse user= invitationService.registerInvitedUser(invitationRegistrationRequest);
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
        String invitedByName= authClient.getUserById(invitation.getInvitedBy()).getName();

        return new InvitationResponse( true,
                message,
                invitation.getId(),
                invitation.getEmail(),
                invitation.getRole(),
                invitation.getStatus(),
                invitedByName,
                invitation.getInvitedBy()
        );
    }
}
