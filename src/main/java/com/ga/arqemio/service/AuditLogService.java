package com.ga.arqemio.service;

import com.ga.arqemio.model.AuditLog;
import com.ga.arqemio.model.Company;
import com.ga.arqemio.model.User;
import com.ga.arqemio.repository.AuditLogRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@AllArgsConstructor
public class AuditLogService {
    private AuditLogRepository auditLogRepository;
    private static final Logger logger= Logger.getLogger(AuditLogService.class.getName());

    public AuditLog createAuditLog(User user, Company company, String action, String entityType, Long entityId, String details){
        AuditLog auditLog=new AuditLog();
        auditLog.setUser(user);
        auditLog.setCompany(company);
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setDetails(details);

        AuditLog savedAuditLog= auditLogRepository.save(auditLog);
        logger.log(Level.INFO, "Audit log {0} created for action {1} on {2} with ID {3}", new Object[]{savedAuditLog.getId(), action, entityType, entityId});
        return savedAuditLog;
    }

    public List<AuditLog> getCompanyAuditLogs(Long companyId){
        return auditLogRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);
    }

    public List<AuditLog> getPlatformAuditLogs(){
        return auditLogRepository.findByActionInOrderByCreatedAtDesc(
                List.of(
                        "CREATE_COMPANY",
                        "CREATE_OWNER",
                        "REGISTER_MANAGER",
                        "REGISTER_WORKER",
                        "REGISTER_CUSTOMER",
                        "REGISTER_USER"
                )
        );
    }
}
