package com.ga.company_service.repository;

import com.ga.company_service.model.Risk;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface RiskRepository extends JpaRepository<Risk, Long> {
    List<Risk> findByProjectId(Long projectId);
    List<Risk> findByProjectIdAndStatus(Long projectId, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Risk> findWithLockById(Long id);
}
