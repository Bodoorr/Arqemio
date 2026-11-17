package com.ga.auth_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuditLogClient {
    private RestClient restClient;

    @Value("${internal.api-key}")
    private String internalApiKey;

    public AuditLogClient(@Value("${notification.service.url}") String notificationServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(notificationServiceUrl).build();
    }

    public void createAuditLog(Long userId, Long companyId, String action, String entityType, Long entityId, String details) {
        Map<String, Object> auditLog = new HashMap<>();
        auditLog.put("userId", userId);
        auditLog.put("companyId", companyId);
        auditLog.put("action", action);
        auditLog.put("entityType", entityType);
        auditLog.put("entityId", entityId);
        auditLog.put("details", details);

        restClient.post()
                .uri("/audit-logs/create")
                .header("X-Internal-Api-Key", internalApiKey)
                .body(auditLog)
                .retrieve()
                .body(Void.class);
    }
}