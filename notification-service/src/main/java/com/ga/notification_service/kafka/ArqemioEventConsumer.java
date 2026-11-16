package com.ga.notification_service.kafka;

import com.ga.notification_service.event.ArqemioEvent;
import com.ga.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArqemioEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "arqemio-events", groupId = "arqemio-notification-group")
    public void consume(ArqemioEvent event) {
        System.out.println("Received Kafka event: " + event);

        if (event.userId() != null && event.message() != null){
            notificationService.sendNotification(event.userId(), event.message());
        }
    }
}