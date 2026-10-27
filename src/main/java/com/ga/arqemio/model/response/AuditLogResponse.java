package com.ga.arqemio.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
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
