package com.ga.notification_service.service;

import com.ga.notification_service.model.AuditLog;
import com.ga.notification_service.model.response.UserResponse;
import com.ga.notification_service.repository.AuditLogRepository;
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

//    public static UserResponse getCurrentLoggedInUser(){
//        return (UserResponse) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//    }

    public AuditLog createAuditLog(Long userId, Long companyId, String action, String entityType, Long entityId, String details){
        AuditLog auditLog=new AuditLog();
        auditLog.setUserId(userId);
        auditLog.setCompanyId(companyId);
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setDetails(details);

        AuditLog savedAuditLog= auditLogRepository.save(auditLog);
        logger.log(Level.INFO, "Audit log {0} created for action {1} on {2} with ID {3}", new Object[]{savedAuditLog.getId(), action, entityType, entityId});
        return savedAuditLog;
    }

//    public List<AuditLog> getCompanyAuditLogs(Long companyId){
//        UserResponse currentUser= getCurrentLoggedInUser();
//
//        boolean isOwner= companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(currentUser.getId(), companyId, "OWNER","ACTIVE");
//        if (!isOwner){
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Access denied.");
//        }
//
//        return auditLogRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);
//    }

//    public List<AuditLog> getPlatformAuditLogs(){
//        UserResponse currentUser= getCurrentLoggedInUser();
//        if (!Boolean.TRUE.equals(currentUser.getIsPlatformAdmin())){
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Access denied.");
//        }
//        return auditLogRepository.findByActionInOrderByCreatedAtDesc(
//                List.of(
//                        "CREATE_COMPANY",
//                        "REGISTER_OWNER",
//                        "REGISTER_MANAGER",
//                        "REGISTER_WORKER",
//                        "REGISTER_CUSTOMER"
//                )
//        );
//    }
}
