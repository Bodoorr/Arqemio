package com.ga.company_service.controller;

import com.ga.company_service.model.Risk;
import com.ga.company_service.model.RiskAcknowledgement;
import com.ga.company_service.model.request.RiskRequest;
import com.ga.company_service.service.RiskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/company/projects/risks")
@RequiredArgsConstructor
public class RiskController {
    private final RiskService riskService;

    @PostMapping
    public ResponseEntity<Risk> reportRisk(@Valid @RequestBody RiskRequest riskRequest){
        return ResponseEntity.ok(riskService.reportRisk(riskRequest));
    }

    @PutMapping("/{riskId}/approve")
    public ResponseEntity<Risk> approveRisk(@PathVariable Long riskId, @RequestParam Integer likelihood, @RequestParam Integer impact, @RequestParam(defaultValue = "false") Boolean evacuationRequired){
        return  ResponseEntity.ok(riskService.approveRisk(riskId,likelihood,impact, evacuationRequired));
    }

    @PutMapping("/{riskId}/reject")
    public ResponseEntity<Risk> rejectRisk(@PathVariable Long riskId){
        return ResponseEntity.ok(riskService.rejectRisk(riskId));
    }

    @PutMapping("/{riskId}/assign")
    public ResponseEntity<Risk> assignRisk(@PathVariable Long riskId, @RequestParam Long userId, @RequestParam String action) {
        return ResponseEntity.ok(riskService.assignRisk(riskId, userId, action));
    }

    @PutMapping("/{riskId}/resolve")
    public ResponseEntity<Risk> resolveRisk(@PathVariable Long riskId, @RequestParam LocalDateTime resolvedAt) {
        return ResponseEntity.ok(riskService.resolveRisk(riskId, resolvedAt));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Risk>> getRisksByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(riskService.getRisksByProject(projectId));
    }

    @PutMapping("/{riskId}/acknowledge")
    public ResponseEntity<RiskAcknowledgement> acknowledgeRisk(@PathVariable Long riskId, @RequestParam String status){
        return ResponseEntity.ok(riskService.acknowledgeRisk(riskId, status));
    }
}
