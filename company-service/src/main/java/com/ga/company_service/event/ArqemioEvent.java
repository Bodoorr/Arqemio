package com.ga.company_service.event;

import java.time.LocalDateTime;

public record ArqemioEvent(String eventId, Long userId, Long companyId, String action, String entityType,
                           Long entityId, String message, LocalDateTime createdAt, Boolean evacuationRequired) {

    public ArqemioEvent(String eventId, Long userId, Long companyId, String action, String entityType,
            Long entityId, String message, LocalDateTime createdAt) {
        this(eventId, userId, companyId, action, entityType,
                entityId, message, createdAt, null);
    }
}
