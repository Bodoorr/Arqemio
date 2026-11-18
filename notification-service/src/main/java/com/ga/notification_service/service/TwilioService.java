package com.ga.notification_service.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.twilio.type.Twiml;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwilioService {
    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String twilioPhoneNumber;

    @PostConstruct
    public void initialize() {
        Twilio.init(accountSid, authToken);
    }

    public void sendSms(String mobileNumber, String message) {
        Message.creator(
                new PhoneNumber(mobileNumber),
                new PhoneNumber(twilioPhoneNumber),
                message
        ).create();
    }

    public void makeCall(String mobileNumber, String message) {

        String twiml = new com.twilio.twiml.VoiceResponse.Builder()
                .say(new com.twilio.twiml.voice.Say.Builder(message).build())
                .build()
                .toXml();

        Call.creator(
                new PhoneNumber(mobileNumber),
                new PhoneNumber(twilioPhoneNumber),
                new Twiml(twiml)
        ).create();
    }


}

