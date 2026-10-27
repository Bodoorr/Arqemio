package com.ga.arqemio.model.response;

import java.time.LocalDateTime;

public class AuditLogResponse {
    Long id;
    Long userId;
    String userName;
    Long companyId;
    String action;
    String entityType;
    Long entityId;
    String details;
    LocalDateTime createdAt;
}
