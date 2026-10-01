package com.ga.arqemio.service;

import com.ga.arqemio.model.EmailDetails;

public interface EmailService {
    //to send simple email
    String sendSimpleMail(EmailDetails details);

    //to send email with attachment
    String sendMailWithAttachment(EmailDetails details);
}
