package com.ga.company_service.repository;

import com.ga.company_service.model.RiskAcknowledgement;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface RiskAcknowledgementRepository extends JpaRepository<RiskAcknowledgement,Long> {
    Optional<RiskAcknowledgement> findByRiskIdAndWorkerId(Long riskId, Long workerId);
    List<RiskAcknowledgement> findByRiskId(Long riskId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RiskAcknowledgement> findByCallToken(String callToken);
}
