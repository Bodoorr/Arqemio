package com.ga.arqemio.service;

import com.ga.arqemio.model.AuditLog;
import com.ga.arqemio.model.Company;
import com.ga.arqemio.model.User;
import com.ga.arqemio.repository.AuditLogRepository;
import com.ga.arqemio.repository.CompanyMembershipRepository;
import com.ga.arqemio.security.MyUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@AllArgsConstructor
public class AuditLogService {
    private AuditLogRepository auditLogRepository;
    private static final Logger logger= Logger.getLogger(AuditLogService.class.getName());
    private CompanyMembershipRepository companyMembershipRepository;

    public static User getCurrentLoggedInUser(){
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

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
        User currentUser= getCurrentLoggedInUser();

        boolean isOwner= companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(currentUser.getId(), companyId, "OWNER","ACTIVE");
        if (!isOwner){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Access denied.");
        }

        return auditLogRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);
    }

    public List<AuditLog> getPlatformAuditLogs(){
        User currentUser= getCurrentLoggedInUser();
        if (!Boolean.TRUE.equals(currentUser.getIsPlatformAdmin())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Access denied.");
        }
        return auditLogRepository.findByActionInOrderByCreatedAtDesc(
                List.of(
                        "CREATE_COMPANY",
                        "CREATE_OWNER",
                        "REGISTER_MANAGER",
                        "REGISTER_WORKER",
                        "REGISTER_CUSTOMER"
                )
        );
    }
}
