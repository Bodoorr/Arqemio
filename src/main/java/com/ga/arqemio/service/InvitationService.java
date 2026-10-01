package com.ga.arqemio.service;

import com.ga.arqemio.model.EmailDetails;
import com.ga.arqemio.model.Invitation;
import com.ga.arqemio.model.User;
import com.ga.arqemio.repository.InvitationRepository;
import com.ga.arqemio.security.MyUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class InvitationService {
    private InvitationRepository invitationRepository;
    private EmailService emailService;

    public User getCurrentLoggedInUser(){
        MyUserDetails userDetails= (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    public Invitation createInvitation(String email, String role){
        User currentUser= getCurrentLoggedInUser();

        if (!currentUser.getIsPlatformAdmin().equals(true) ){
            throw new RuntimeException("Only Platform Admin can invite company owners.");
        }

        if (!role.equals("OWNER")){
            throw new RuntimeException("Platform Admin can only invite company owners.");
        }
        Invitation invitation= new Invitation();

        invitation.setEmail(email);
        invitation.setRole(role);
        invitation.setUser(currentUser);

        invitation.setCompany(null);

        invitation.setToken(UUID.randomUUID().toString());

        invitation.setStatus("PENDING");

        invitation.setExpiresAt(LocalDateTime.now().plusHours(48));


        Invitation savedInvitation= invitationRepository.save(invitation);

        EmailDetails emailDetails=new EmailDetails();
        emailDetails.setRecipient(email);
        emailDetails.setSubject("You're invited to join Arqemio!");
        emailDetails.setMsgBody(
                "Hello!\n\n" +
                        "You have been invited to join Arqemio as a Company Owner.\n\n" +
                        "Your invitation token is:\n" +
                        savedInvitation.getToken() + "\n\n" +
                        "This invitation expires in 48 hours.\n\n" +
                        "Arqemio Team"
        );

        String result= emailService.sendSimpleMail(emailDetails);

        if (!result.equals(""))
        return savedInvitation;
    }



}
