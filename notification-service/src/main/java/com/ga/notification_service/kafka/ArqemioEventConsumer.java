package com.ga.notification_service.kafka;

import com.ga.notification_service.event.ArqemioEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ArqemioEventConsumer {

    @KafkaListener(topics = "arqemio-events", groupId = "arqemio-notification-group")
    public void consume(ArqemioEvent event) {
        System.out.println("Received Kafka event: " + event);
    }
}