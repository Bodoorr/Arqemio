package com.ga.arqemio.service;

import com.ga.arqemio.model.AuditLog;
import com.ga.arqemio.model.Company;
import com.ga.arqemio.model.User;
import com.ga.arqemio.repository.AuditLogRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuditLogService {
    private AuditLogRepository auditLogRepository;

    public AuditLog createAuditLog(User user, Company company, String action, String entityType, Long entityId, String details){
        AuditLog auditLog=new AuditLog();
        auditLog.setUser(user);
        auditLog.setCompany(company);
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setDetails(details);

        return auditLogRepository.save(auditLog);
    }
}
