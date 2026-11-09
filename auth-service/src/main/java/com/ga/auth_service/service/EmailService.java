package com.ga.auth_service.service;

import com.ga.auth_service.model.EmailDetails;

public interface EmailService {
    //to send simple email
    boolean sendSimpleMail(EmailDetails details);

    //to send email with attachment
    boolean sendMailWithAttachment(EmailDetails details);

    boolean sendHtmlMail(EmailDetails emailDetails);
}
