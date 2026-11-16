package com.ga.company_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class NotificationClient {
    private RestClient restClient;

    @Value("${internal.api-key}")
    private String internalApiKey;

    public NotificationClient(@Value("${notification.service.url}") String notificationServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(notificationServiceUrl).build();
    }

    public Boolean sendNotification(Long userId, String message) {
        return restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/notifications/send")
                        .queryParam("userId", userId)
                        .queryParam("message", message)
                        .build())
                .header("X-Internal-Api-Key", internalApiKey)
                .retrieve()
                .body(Boolean.class);
    }
}