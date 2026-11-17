package com.ga.company_service.repository;

import com.ga.company_service.model.Risk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiskRepository extends JpaRepository<Risk, Long> {
    List<Risk> findByProjectId(Long projectId);
    List<Risk> findByProjectIdAndStatus(Long projectId, String status);
}
