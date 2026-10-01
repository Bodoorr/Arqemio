package com.ga.arqemio.controller;

import com.ga.arqemio.model.EmailDetails;
import com.ga.arqemio.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test/emails")
public class EmailController {
    @Autowired
    private EmailService emailService;

    //Send simple email
    @PostMapping("/send")
    public String sendMail(@RequestBody EmailDetails details){
        return emailService.sendSimpleMail(details);
    }

    //send email with attachment
    @PostMapping("/sendMailWithAttachment")
    public String sendMailWithAttachment(
            @RequestBody EmailDetails details
    ){
        return emailService.sendMailWithAttachment(details);
    }

}
