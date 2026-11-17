package com.ga.company_service.service;

import com.ga.company_service.client.AuditLogClient;
import com.ga.company_service.event.ArqemioEvent;
import com.ga.company_service.kafka.ArqemioEventProducer;
import com.ga.company_service.model.CompanyMembership;
import com.ga.company_service.model.Project;
import com.ga.company_service.model.Risk;
import com.ga.company_service.model.request.RiskRequest;
import com.ga.company_service.model.response.UserResponse;
import com.ga.company_service.repository.CompanyMembershipRepository;
import com.ga.company_service.repository.RiskRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@AllArgsConstructor
public class RiskService {
    private static final Logger logger= Logger.getLogger(RiskService.class.getName());
    private RiskRepository riskRepository;
    private CompanyMembershipRepository companyMembershipRepository;
    private ProjectService projectService;
    private AuditLogClient auditLogClient;
    private ArqemioEventProducer arqemioEventProducer;

    @Transactional
    public Risk reportRisk(RiskRequest riskRequest){
        UserResponse currentUser= ProjectService.getCurrentLoggedInUser();

        if (riskRequest == null || riskRequest.getProjectId() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project ID is required.");
        }

        Project project= projectService.getProjectById(riskRequest.getProjectId());
        CompanyMembership membership= companyMembershipRepository.findByUserIdAndCompanyIdAndStatus(
                currentUser.getId(), project.getCompany().getId(), "ACTIVE")
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.FORBIDDEN, "Active company membership not found."));

        Risk risk= new Risk();
        risk.setProject(project);
        risk.setTitle(riskRequest.getTitle());
        risk.setLocation(riskRequest.getLocation());
        risk.setCategory(riskRequest.getCategory());
        risk.setReportedBy(membership);
        risk.setStatus("PENDING_REVIEW");

        Risk savedRisk= riskRepository.save(risk);
        auditLogClient.createAuditLog(currentUser.getId(), project.getCompany().getId(), "CREATE","RISK", savedRisk.getId(), "Reported risk: "+ savedRisk.getTitle());
        logger.log(Level.INFO, "Risk {0} reported in project {1} by user {2}", new Object[]{savedRisk.getId(), project.getId(), currentUser.getId()});

        List<Long> managerIds = new ArrayList<>();

        for (CompanyMembership manager : project.getManagers()){
            if (manager.getStatus().equals("ACTIVE") && manager.getRole().equals("MANAGER") && !manager.getUserId().equals(currentUser.getId()) && !managerIds.contains(manager.getUserId())){
                managerIds.add(manager.getUserId());
            }
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit(){
                for (Long managerId : managerIds){
                    ArqemioEvent event= new ArqemioEvent(
                        UUID.randomUUID().toString(),
                        managerId,
                        project.getCompany().getId(),
                        "RISK_REPORTED",
                        "RISK",
                        savedRisk.getId(),
                        "New risk reported in project "+ project.getName()+": "+savedRisk.getTitle(), LocalDateTime.now()
                );
            arqemioEventProducer.sendEvent(event);
                }
            }
        });
        return savedRisk;
    }
}
