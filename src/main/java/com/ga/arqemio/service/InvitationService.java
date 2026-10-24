package com.ga.arqemio.service;

import com.ga.arqemio.model.*;
import com.ga.arqemio.model.request.InvitationRegistrationRequest;
import com.ga.arqemio.repository.CompanyMembershipRepository;
import com.ga.arqemio.repository.CompanyRepository;
import com.ga.arqemio.repository.InvitationRepository;
import com.ga.arqemio.repository.UserRepository;
import com.ga.arqemio.security.MyUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    private AuditLogService auditLogService;

    public User getCurrentLoggedInUser(){
        MyUserDetails userDetails= (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    public String getInvitationEmail(
            String companyName,
            String role,
            String invitationLink
    ) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/invitation-email.html");
            String html = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            html = html.replace("{{companyName}}", companyName);
            html = html.replace("{{role}}", role);
            html = html.replace("{{invitationLink}}", invitationLink);
            return html;

        } catch (IOException e) {
            throw new RuntimeException("Could not load invitation email.");
        }
    }

    public Invitation createInvitation(String email, String role, Long companyId) {
        User currentUser = getCurrentLoggedInUser();
        Company company= companyRepository.findById(companyId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found."));

        if (!"OWNER".equals(role) &&
                        !"MANAGER".equals(role) &&
                        !"WORKER".equals(role)
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid role. Role must be OWNER, MANAGER, or WORKER."
            );
        }

        boolean isPlatformAdmin = Boolean.TRUE.equals(currentUser.getIsPlatformAdmin());
        boolean isCompanyOwner= companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(currentUser.getId(), companyId,"OWNER", "ACTIVE");

        if ("OWNER".equals(role)){
            if (!isPlatformAdmin){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only platform admins can invite company owners.");
                }
            } else {
            if (!isCompanyOwner){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only company owners can invite managers and workers.");
            }
        }

        boolean alreadyAccepted =
                invitationRepository.existsByEmailIgnoreCaseAndCompanyIdAndRoleAndStatus(
                        email,
                        companyId,
                        role,
                        "ACCEPTED"
                );

        if (alreadyAccepted) {
            throw new RuntimeException(
                    "This email has already accepted an invitation for this role."
            );
        }

        boolean alreadyPending =
                invitationRepository.existsByEmailIgnoreCaseAndCompanyIdAndRoleAndStatus(
                        email,
                        companyId,
                        role,
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

       boolean isEmailSent= sendInvitationEmail(savedInvitation);
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

    public User registerInvitedUser(InvitationRegistrationRequest invitationRegistrationRequest){
        Invitation invitation= validateInvitation(invitationRegistrationRequest.getToken());

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

            CompanyMembership savedMembership= createMembership(savedUser, invitation);

            updateCompanyOwner(invitation, savedMembership);
            markInvitationAccepted(invitation, savedUser);
        return savedUser;
    }


    public Invitation acceptInvitation(String token){
        User currentUser= getCurrentLoggedInUser();
        Invitation invitation= validateInvitation(token);

        if (!currentUser.getEmail().equalsIgnoreCase(invitation.getEmail())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This invitation does not belong to the logged in user.");
        }

        boolean alreadyMember= companyMembershipRepository.existsByUserIdAndCompanyIdAndStatus(currentUser.getId(), invitation.getCompany().getId(), "ACTIVE");
        if (alreadyMember){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You are already an active member of this company.");
        }

        CompanyMembership savedMembership= createMembership(currentUser, invitation);
        updateCompanyOwner(invitation, savedMembership);

       return markInvitationAccepted(invitation, currentUser);
    }

    public Invitation cancelInvitation(Long invitationId){
        User currentUser= getCurrentLoggedInUser();
        Invitation invitation= findInvitationById(invitationId);

        if (!canManageInvitation(currentUser,invitation)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to cancel the invitation.");
        }

        if (!invitation.getStatus().equals("PENDING")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't cancel this invitation.");
        }

        invitation.setStatus("CANCELLED");
        invitationRepository.save(invitation);

        return invitation;
    }

    public Invitation resendInvitation(Long invitationId){
        User currentUser= getCurrentLoggedInUser();
        Invitation invitation= findInvitationById(invitationId);

        if (!canManageInvitation(currentUser, invitation)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to resend the invitation.");

        }
        if (invitation.getStatus().equals("ACCEPTED")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't resend this invitation.");
        }

        invitation.setToken(UUID.randomUUID().toString());
        invitation.setStatus("PENDING");
        invitation.setExpiresAt(LocalDateTime.now().plusHours(48));
        invitationRepository.save(invitation);

        boolean isEmailSent= sendInvitationEmail(invitation);
        if (!isEmailSent){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invitation updated, but email could not be sent.");
        }

        return invitation;
    }

    private boolean canManageInvitation(User user, Invitation invitation){
        if ("OWNER".equals(invitation.getRole())){
            return Boolean.TRUE.equals(user.getIsPlatformAdmin());
        }
        return companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(user.getId(), invitation.getCompany().getId(), "OWNER","ACTIVE");
    }

    private Invitation findInvitationById(Long invitationId){
        return invitationRepository.findById(invitationId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found."));
    }

    private boolean sendInvitationEmail(Invitation invitation){
        String invitationLink = "http://localhost:5173/invitation/" + invitation.getToken();
        String emailBody = getInvitationEmail(invitation.getCompany().getName(), invitation.getRole(), invitationLink);

        EmailDetails emailDetails = new EmailDetails();

        emailDetails.setRecipient(invitation.getEmail());
        emailDetails.setSubject("You're invited to join Arqemio!");
        emailDetails.setMsgBody(emailBody);

        return emailService.sendHtmlMail(emailDetails);
    }

    private CompanyMembership createMembership(User user, Invitation invitation){
        CompanyMembership membership= new CompanyMembership();
        membership.setUser(user);
        membership.setCompany(invitation.getCompany());
        membership.setRole(invitation.getRole());
        membership.setStatus("ACTIVE");

        return companyMembershipRepository.save(membership);
    }

    private void updateCompanyOwner(Invitation invitation, CompanyMembership membership){
        if ("OWNER".equals(invitation.getRole())){
            Company company= invitation.getCompany();
            company.setOwner(membership);
            companyRepository.save(company);
        }
    }

    private Invitation markInvitationAccepted(Invitation invitation, User user){
        invitation.setStatus("ACCEPTED");
        Invitation savedInvitation = invitationRepository.save(invitation);
        auditLogService.createAuditLog(user, invitation.getCompany(),
                "ACCEPT", "INVITATION", savedInvitation.getId(),
                "Accepted invitation as " + invitation.getRole()
        );

        return savedInvitation;
    }
}
