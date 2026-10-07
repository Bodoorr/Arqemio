package com.ga.arqemio.service;

import com.ga.arqemio.model.EmailDetails;

public interface EmailService {
    //to send simple email
    boolean sendSimpleMail(EmailDetails details);

    //to send email with attachment
    boolean sendMailWithAttachment(EmailDetails details);

    boolean sendHtmlMail(EmailDetails emailDetails);
}
