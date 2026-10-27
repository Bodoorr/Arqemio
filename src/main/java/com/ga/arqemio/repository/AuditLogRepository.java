package com.ga.arqemio.repository;

import com.ga.arqemio.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    //company owner
    List<AuditLog> findByCompanyIdOrderByCreatedAtDesc(Long companyId);
    //Platform admin logs
    List<AuditLog> findByActionInOrderByCreatedAtDesc(List<String> actions);
}
