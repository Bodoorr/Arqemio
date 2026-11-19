package com.ga.notification_service.kafka;

import com.ga.notification_service.client.AuthClient;
import com.ga.notification_service.client.CompanyClient;
import com.ga.notification_service.event.ArqemioEvent;
import com.ga.notification_service.model.response.UserProfileResponse;
import com.ga.notification_service.service.NotificationService;
import com.ga.notification_service.service.TwilioService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ArqemioEventConsumer {

    private final NotificationService notificationService;
    private final AuthClient authClient;
    private final TwilioService twilioService;
    private final CompanyClient companyClient;
    private final Map<String, Boolean> processedEvents = new ConcurrentHashMap<>();
    private final Map<String, Boolean> sentNotifications = new ConcurrentHashMap<>();
    private final Map<String, Boolean> sentSms = new ConcurrentHashMap<>();

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${arqemio.notification-public-url}")
    private String notificationPublicUrl;

    @KafkaListener(topics = "arqemio-events", groupId = "arqemio-notification-group")
    public synchronized void consume(ArqemioEvent event) {
        System.out.println("Received Kafka event: " + event);

        if (event.userId() == null || event.message() == null || event.eventId() == null){
            return;
        }

        if (processedEvents.containsKey(event.eventId())) {
            System.out.println("Duplicate Kafka event ignored: " + event.eventId());
            return;
        }

        if (!sentNotifications.containsKey(event.eventId())) {
            notificationService.sendNotification(event.userId(), event.message());
            sentNotifications.put(event.eventId(), true);
        }

        if ("RISK_REPORTED".equals(event.action()) || "CRITICAL_RISK_ALERT".equals(event.action()) || "WORKER_NEEDS_ASSISTANCE".equals(event.action())) {
            UserProfileResponse user = authClient.getUserById(event.userId());

            if (user == null) {
                throw new IllegalStateException("User not found: " + event.userId());
            }

            String subject = "Arqemio - New Risk Reported";
            if ("CRITICAL_RISK_ALERT".equals(event.action())) {
                subject = "Arqemio - CRITICAL RISK ALERT";
            } else if ("WORKER_NEEDS_ASSISTANCE".equals(event.action())) {
                subject = "Arqemio - URGENT Worker Assistance Required";
            }

            if (user.getMobileNumber() != null && !user.getMobileNumber().isBlank()) {
                String riskLink = frontendUrl + "/risks/" + event.entityId();
                String smsMessage = null;

                if ("RISK_REPORTED".equals(event.action())) {
                    smsMessage = "ARQEMIO | Risk Approval Required\n" + event.message()
                            + "\nReview and approve or reject:\n" + riskLink
                            + "\nيرجى مراجعة بلاغ الخطر عبر الرابط.";
                } else if ("CRITICAL_RISK_ALERT".equals(event.action())) {
                    smsMessage = "ARQEMIO | CRITICAL SAFETY ALERT\n" + event.message();
                    if (Boolean.TRUE.equals(event.evacuationRequired())) {
                        smsMessage += "\nUpdate your safety status:\n" + riskLink + "/acknowledge" + "\nيرجى تحديث حالة سلامتك عبر الرابط.";
                    } else {
                        smsMessage += "\nView the risk:\n" + riskLink + "\nيرجى الاطلاع على بلاغ الخطر عبر الرابط.";
                    }
                } else if ("WORKER_NEEDS_ASSISTANCE".equals(event.action())) {
                    smsMessage = "ARQEMIO | URGENT ASSISTANCE\n" + event.message()
                            + "\nView the risk:\n" + riskLink
                            + "\nيوجد عامل بحاجة إلى مساعدة فورية.";
                }

                if (smsMessage != null && !sentSms.containsKey(event.eventId())) {
                    try {
                        twilioService.sendSms(user.getMobileNumber(), smsMessage);
                        sentSms.put(event.eventId(), true);
                    } catch (Exception e) {
                        System.err.println("SMS failed for event " + event.eventId());
                    }
                }

                if ("RISK_REPORTED".equals(event.action())) {
                    twilioService.makeManagerCall(user.getMobileNumber());
                } else if ("WORKER_NEEDS_ASSISTANCE".equals(event.action())) {
                    twilioService.makeEmergencyManagerCall(user.getMobileNumber());
                }

                if ("CRITICAL_RISK_ALERT".equals(event.action())) {
                    String riskTitle = event.message();
                    String riskLocation = "Not specified";

                    String[] details = event.message().split("\\|");
                    if (details.length > 0) {
                        riskTitle = details[0].replace("CRITICAL RISK:", "").trim();
                    }

                    if (details.length > 1) {
                        riskLocation = details[1].replace("Location:", "").trim();
                    }

                    boolean evacuationRequired = Boolean.TRUE.equals(event.evacuationRequired());
                    if (evacuationRequired) {
                        String callToken = companyClient.getRiskCallToken(event.entityId(), event.userId());
                        if (callToken != null && !callToken.isBlank()) {
                            boolean claimed = companyClient.claimRiskCall(callToken);

                            if (claimed) {
                                String callbackUrl = notificationPublicUrl + "/notifications/twilio/response?callToken=" + callToken;
                                String callSid = twilioService.makeWorkerCall(user.getMobileNumber(), riskTitle, riskLocation, callbackUrl, callToken);
                                companyClient.registerRiskCall(callToken, callSid);
                            }
                        }
                    }
                }
            }
            if (user.getEmail() != null && !user.getEmail().isBlank()) {
                try {
                    authClient.sendEmail(user.getEmail(), subject, event.message());
                } catch (Exception e) {
                    System.err.println("Email failed for event " + event.eventId());
                }
            }
        }
        processedEvents.put(event.eventId(), true);
    }

}