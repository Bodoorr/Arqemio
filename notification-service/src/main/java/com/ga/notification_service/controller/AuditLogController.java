package com.ga.notification_service.controller;

import com.ga.notification_service.client.AuthClient;
import com.ga.notification_service.model.AuditLog;
import com.ga.notification_service.model.response.AuditLogResponse;
import com.ga.notification_service.model.response.UserProfileResponse;
import com.ga.notification_service.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {
    private AuditLogService auditLogService;
    private AuthClient authClient;

    @Value("${internal.api-key}")
    private String internalApikey;

    @GetMapping("/admin")
    public ResponseEntity<List<AuditLogResponse>> getPlatformAuditLogs(){
        List<AuditLog> auditLogs= auditLogService.getPlatformAuditLogs();
        List<AuditLogResponse> responses= new ArrayList<>();

        for (AuditLog auditLog: auditLogs){
            responses.add(toResponse(auditLog));
        }
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<AuditLogResponse>> getCompanyAuditLogs(@PathVariable Long companyId){
        List<AuditLog> auditLogs= auditLogService.getCompanyAuditLogs(companyId);
        List<AuditLogResponse> responses= new ArrayList<>();

        for (AuditLog auditLog: auditLogs){
            responses.add(toResponse(auditLog));
        }
        return  ResponseEntity.ok(responses);
    }

    @PostMapping("/create")
    public ResponseEntity<AuditLog> createAuditLog(@RequestHeader("X-Internal-Api-Key") String apiKey, @RequestBody AuditLog auditLog){
        if (internalApikey.isBlank() || !internalApikey.equals(apiKey)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API key");
        }

        AuditLog savedAuditLog= auditLogService.createAuditLog(
                auditLog.getUserId(),
                auditLog.getCompanyId(),
                auditLog.getAction(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getDetails()
        );
        return ResponseEntity.ok(savedAuditLog);
    }

    private AuditLogResponse toResponse(AuditLog auditLog){
        UserProfileResponse user= authClient.getUserById(auditLog.getUserId());
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getUserId(),
                user.getName(),
                auditLog.getCompanyId(),
                auditLog.getAction(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getDetails(),
                auditLog.getCreatedAt()
        );
    }

}



