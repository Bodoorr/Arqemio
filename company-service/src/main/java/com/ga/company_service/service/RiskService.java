package com.ga.company_service.service;

import com.ga.company_service.client.AuditLogClient;
import com.ga.company_service.model.Risk;
import com.ga.company_service.repository.CompanyMembershipRepository;
import com.ga.company_service.repository.RiskRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RiskService {
    private RiskRepository riskRepository;
    private CompanyMembershipRepository companyMembershipRepository;
    private ProjectService projectService;
    private AuditLogClient auditLogClient;

    public Risk reportRisk(Long projectId, Risk riskRequest){

    }
}
