package com.ga.arqemio.repository;

import com.ga.arqemio.security.EmailDetails;

public interface EmailRepository {
    //to send simple email
    String sendSimpleMail(EmailDetails details);

    //to send email with attachment
    String sendMailWithAttachment(EmailDetails details);
}
