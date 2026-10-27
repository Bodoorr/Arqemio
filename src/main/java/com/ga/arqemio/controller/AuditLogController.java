package com.ga.arqemio.controller;

import com.ga.arqemio.model.AuditLog;
import com.ga.arqemio.model.response.AuditLogResponse;
import com.ga.arqemio.service.AuditLogService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/audit-logs")
@AllArgsConstructor
public class AuditLogController {
    private AuditLogService auditLogService;

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

    private AuditLogResponse toResponse(AuditLog auditLog){
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getUser().getId(),
                auditLog.getUser().getName(),
                auditLog.getCompany().getId(),
                auditLog.getAction(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getDetails(),
                auditLog.getCreatedAt()
        );
    }

}



