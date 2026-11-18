package com.ga.notification_service.controller;

import com.ga.notification_service.client.CompanyClient;
import com.twilio.security.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notifications/twilio")
@RequiredArgsConstructor
public class TwilioController {
    private final CompanyClient companyClient;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${arqemio.notification-public-url}")
    private String publicUrl;

    @PostMapping(value = "/response", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> handleResponse(@RequestParam Map<String, String> parameters, @RequestHeader(value = "X-Twilio-Signature", required = false) String signature) {
        String callToken = parameters.get("callToken");
        String callSid = parameters.get("CallSid");
        String digit = parameters.get("Digits");

        String callbackUrl = publicUrl + "/notifications/twilio/response?callToken=" + callToken;
        Map<String, String> twilioParameters = new java.util.HashMap<>(parameters);
        twilioParameters.remove("callToken");

        RequestValidator validator = new RequestValidator(authToken);
        if (signature == null || !validator.validate(callbackUrl, twilioParameters, signature)) {
            return ResponseEntity.status(403).build();
        }

        String status;
        if ("1".equals(digit)) {
            status = "SAFE";
        } else if ("2".equals(digit)) {
            status = "NEED_ASSISTANCE";
        } else {
            return ResponseEntity.ok(
                    "<Response><Say>Invalid selection.</Say></Response>");
        }

        companyClient.registerRiskCall(callToken, callSid);
        companyClient.acknowledgeRiskPhoneResponse(callToken, callSid, status);
        return ResponseEntity.ok(
                "<Response><Say>Thank you. Your response has been recorded.</Say></Response>");
    }

}
