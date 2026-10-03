package com.ga.arqemio.service;

import com.ga.arqemio.model.Company;
import com.ga.arqemio.model.EmailDetails;
import com.ga.arqemio.model.Invitation;
import com.ga.arqemio.model.User;
import com.ga.arqemio.repository.CompanyRepository;
import com.ga.arqemio.repository.InvitationRepository;
import com.ga.arqemio.security.MyUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class InvitationService {
    private InvitationRepository invitationRepository;
    private EmailService emailService;
    private CompanyRepository companyRepository;

    public User getCurrentLoggedInUser(){
        MyUserDetails userDetails= (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    public Invitation createInvitation(String email, String role, Long companyId) {
        User currentUser = getCurrentLoggedInUser();

        if (!currentUser.getIsPlatformAdmin().equals(true)) {
            throw new RuntimeException("Only Platform Admin can invite company owners.");
        }

        if (!role.equals("OWNER")) {
            throw new RuntimeException("Platform Admin can only invite company owners.");
        }

        Company company= companyRepository.findById(companyId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found."));

        boolean alreadyAccepted =
                invitationRepository.existsByEmailIgnoreCaseAndRoleAndStatus(
                        email,
                        "OWNER",
                        "ACCEPTED"
                );

        if (alreadyAccepted) {
            throw new RuntimeException(
                    "This email has already accepted an owner invitation."
            );
        }

        boolean alreadyPending =
                invitationRepository.existsByEmailIgnoreCaseAndRoleAndStatus(
                        email,
                        "OWNER",
                        "PENDING"
                );

        if (alreadyPending) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This email already has a pending invitation."
            );
        }

        Invitation invitation = new Invitation();

        invitation.setEmail(email);
        invitation.setRole(role);
        invitation.setUser(currentUser);

        invitation.setCompany(company);

        invitation.setToken(UUID.randomUUID().toString());

        invitation.setStatus("PENDING");

        invitation.setExpiresAt(LocalDateTime.now().plusHours(48));


        Invitation savedInvitation = invitationRepository.save(invitation);

        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setRecipient(email);
        emailDetails.setSubject("You're invited to join Arqemio!");
        emailDetails.setMsgBody(
                "Hello!\n\n" +
                        "You have been invited to join " +
                        company.getName() +
                        " on Arqemio as a Company Owner.\n\n" +
                        "Your invitation token is:\n" +
                        savedInvitation.getToken() + "\n\n" +
                        "This invitation expires in 48 hours.\n\n" +
                        "Arqemio Team"
        );

        boolean isEmailSent = emailService.sendSimpleMail(emailDetails);

        if (!isEmailSent) {
            throw new RuntimeException("Invitation saved, but email could not be sent.");
        }

        return savedInvitation;
    }

    public Invitation validateInvitation(String token){
        Invitation invitation= invitationRepository.findByToken(token).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found."));

        if (!invitation.getStatus().equals("PENDING")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "This invitation is no longer available.");
        }
        if (!invitation.getExpiresAt().isAfter(LocalDateTime.now())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "This invitation has expired.");
        }
        return invitation;
    }


}
