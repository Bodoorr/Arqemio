package com.ga.company_service.event;

import java.time.LocalDateTime;

public record ArqemioEvent(String eventId, Long userId, Long companyId, String action, String entityType, Long entityId, String message, LocalDateTime createdAt
) {
}
