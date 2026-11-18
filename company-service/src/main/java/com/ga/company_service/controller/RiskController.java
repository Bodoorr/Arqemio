package com.ga.company_service.controller;

import com.ga.company_service.model.Risk;
import com.ga.company_service.model.RiskAcknowledgement;
import com.ga.company_service.model.request.RiskRequest;
import com.ga.company_service.service.RiskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/company/projects/risks")
@RequiredArgsConstructor
public class RiskController {
    private final RiskService riskService;

    @Value("${internal.api-key}")
    private String internalApiKey;

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

    @PutMapping("/internal/acknowledge")
    public ResponseEntity<RiskAcknowledgement> acknowledgeRiskInternal(@RequestHeader("X-Internal-Api-Key") String apiKey, @RequestParam Long riskId, @RequestParam Long workerUserId, @RequestParam String status) {
        if (internalApiKey == null || internalApiKey.isBlank() || !internalApiKey.equals(apiKey)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API key");
        }
        return ResponseEntity.ok(riskService.acknowledgeRiskInternal(riskId, workerUserId, status));
    }

    @PutMapping("/internal/register-call")
    public ResponseEntity<Void> registerRiskCall(@RequestHeader("X-Internal-Api-Key") String apiKey, @RequestParam String callToken, @RequestParam String callSid) {
        if (internalApiKey == null || internalApiKey.isBlank() || !internalApiKey.equals(apiKey)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API key");
        }
        riskService.registerRiskCall(callToken, callSid);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/internal/phone-response")
    public ResponseEntity<RiskAcknowledgement> acknowledgeRiskPhoneResponse(@RequestHeader("X-Internal-Api-Key") String apiKey, @RequestParam String callToken, @RequestParam String callSid, @RequestParam String status) {
        if (internalApiKey == null || internalApiKey.isBlank() || !internalApiKey.equals(apiKey)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API key");
        }
        return ResponseEntity.ok(riskService.acknowledgeRiskByCallToken(callToken, callSid, status));
    }

    @GetMapping("/{riskId}/acknowledgements")
    public ResponseEntity<List<RiskAcknowledgement>> getRiskAcknowledgements(@PathVariable Long riskId) {
        return ResponseEntity.ok(riskService.getRiskAcknowledgements(riskId));
    }
}
