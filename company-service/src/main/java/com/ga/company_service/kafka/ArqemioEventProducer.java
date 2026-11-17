package com.ga.company_service.kafka;

import com.ga.company_service.event.ArqemioEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArqemioEventProducer {

    private final KafkaTemplate<String, ArqemioEvent> kafkaTemplate;

    public void sendEvent(ArqemioEvent event) {
        try {
            kafkaTemplate.send("arqemio-events", event);
            System.out.println("Kafka event submitted: " + event);
        } catch (RuntimeException e) {
            System.out.println("Failed to send Kafka event");
        }
    }
}
