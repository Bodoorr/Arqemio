package com.ga.arqemio.service;

import com.ga.arqemio.model.*;
import com.ga.arqemio.model.request.InvitationRegistrationRequest;
import com.ga.arqemio.repository.CompanyMembershipRepository;
import com.ga.arqemio.repository.CompanyRepository;
import com.ga.arqemio.repository.InvitationRepository;
import com.ga.arqemio.repository.UserRepository;
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
    private CompanyMembershipRepository companyMembershipRepository;
    private UserRepository userRepository;
    private UserService userService;

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
                invitationRepository.existsByEmailIgnoreCaseAndCompanyIdAndRoleAndStatus(
                        email,
                        companyId,
                        "OWNER",
                        "ACCEPTED"
                );

        if (alreadyAccepted) {
            throw new RuntimeException(
                    "This email has already accepted an owner invitation."
            );
        }

        boolean alreadyPending =
                invitationRepository.existsByEmailIgnoreCaseAndCompanyIdAndRoleAndStatus(
                        email,
                        companyId,
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
            invitation.setStatus("EXPIRED");
            invitationRepository.save(invitation);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "This invitation has expired.");
        }
        return invitation;
    }

    public User registerOwner(InvitationRegistrationRequest invitationRegistrationRequest){
        Invitation invitation= validateInvitation(invitationRegistrationRequest.getToken());

        if (!invitation.getRole().equals("OWNER")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This invitation is not an owner invitation.");
        }

        if (userRepository.existsByEmail(invitation.getEmail())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exist. Please login to accept the invitation.");
        }

            User newUser=new User();
            newUser.setName(invitationRegistrationRequest.getName());
            newUser.setEmail(invitation.getEmail());
            newUser.setPassword(invitationRegistrationRequest.getPassword());
            newUser.setProfilePicture(invitationRegistrationRequest.getProfilePicture());
            newUser.setStatus(true);
            newUser.setIsPlatformAdmin(false);

            User savedUser= userService.createUser(newUser);

        CompanyMembership membership=new CompanyMembership();
        membership.setUser(savedUser);
        membership.setCompany(invitation.getCompany());
        membership.setRole(invitation.getRole());
        membership.setStatus("ACTIVE");

        CompanyMembership savedMembership= companyMembershipRepository.save(membership);

        Company company=invitation.getCompany();
        company.setOwner(savedMembership);
        companyRepository.save(company);

        invitation.setStatus("ACCEPTED");
        invitationRepository.save(invitation);

        return savedUser;
    }


    public Invitation acceptInvitation(String token){
        User currentUser= getCurrentLoggedInUser();
        Invitation invitation= validateInvitation(token);

        if (!currentUser.getEmail().equalsIgnoreCase(invitation.getEmail())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This invitation does not belong to the logged in user.");
        }

        CompanyMembership membership=new CompanyMembership();
        membership.setUser(currentUser);
        membership.setCompany(invitation.getCompany());
        membership.setRole(invitation.getRole());
        membership.setStatus("ACTIVE");

        CompanyMembership savedMembership= companyMembershipRepository.save(membership);

        if (invitation.getRole().equals("OWNER")){
            Company company= invitation.getCompany();
            company.setOwner(savedMembership);
            companyRepository.save(company);
        }

        invitation.setStatus("ACCEPTED");

        return invitationRepository.save(invitation);
    }

    public Invitation cancelInvitation(Long invitationId){
        User currentUser= getCurrentLoggedInUser();
        if (!currentUser.getIsPlatformAdmin()){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to cancel the invitation.");
        }

        Invitation invitation= invitationRepository.findById(invitationId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found."));

        if (!invitation.getStatus().equals("PENDING")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't cancel this invitation.");
        }

        invitation.setStatus("CANCELLED");
        invitationRepository.save(invitation);

        return invitation;
    }



}
