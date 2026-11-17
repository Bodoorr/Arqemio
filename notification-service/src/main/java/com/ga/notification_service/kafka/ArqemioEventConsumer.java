package com.ga.notification_service.kafka;

import com.ga.notification_service.client.AuthClient;
import com.ga.notification_service.event.ArqemioEvent;
import com.ga.notification_service.model.response.UserProfileResponse;
import com.ga.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ArqemioEventConsumer {

    private final NotificationService notificationService;
    private final AuthClient authClient;
    private final Map<String, Boolean> processedEvents = new ConcurrentHashMap<>();

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

        notificationService.sendNotification(event.userId(), event.message());

        if ("RISK_REPORTED".equals(event.action())){
            UserProfileResponse user= authClient.getUserById(event.userId());

            if (user==null || user.getEmail()== null || user.getEmail().isBlank()){
                throw new IllegalStateException("Email not found for user "+ event.userId());
            }

            authClient.sendEmail(
                    user.getEmail(),
                    "Arqemio - New Risk Reported",
                    event.message()
            );
        }
        processedEvents.put(event.eventId(), true);
    }

}