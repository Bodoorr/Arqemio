package com.ga.notification_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class CompanyClient {
    private RestClient restClient;

    @Value("${internal.api-key}")
    private String internalApiKey;

    public CompanyClient(@Value("${company.service.url}") String companyServiceUrl){
        this.restClient=RestClient.builder().baseUrl(companyServiceUrl).build();
    }

    public Boolean isCompanyOwner(Long companyId, Long userId) {
        return restClient.get()
                .uri("/company/internal/{companyId}/owners/{userId}", companyId, userId)
                .header("X-Internal-Api-Key", internalApiKey)
                .retrieve()
                .body(Boolean.class);
    }

    public boolean claimRiskCall(String callToken) {
        Boolean claimed = restClient.put()
                .uri("/company/projects/risks/internal/claim-call?callToken={callToken}", callToken)
                .header("X-Internal-Api-Key", internalApiKey)
                .retrieve()
                .body(Boolean.class);

        return Boolean.TRUE.equals(claimed);
    }

    public void acknowledgeRisk(Long riskId, Long workerUserId, String status) {
        restClient.put()
                .uri("/company/projects/risks/internal/acknowledge?riskId={riskId}&workerUserId={workerUserId}&status={status}", riskId, workerUserId, status)
                .header("X-Internal-Api-Key", internalApiKey)
                .retrieve()
                .toBodilessEntity();
    }

    public void registerRiskCall(String callToken, String callSid) {
        restClient.put()
                .uri("/company/projects/risks/internal/register-call?callToken={callToken}&callSid={callSid}", callToken, callSid)
                .header("X-Internal-Api-Key", internalApiKey)
                .retrieve()
                .toBodilessEntity();
    }

    public void acknowledgeRiskPhoneResponse(String callToken, String callSid, String status) {
        restClient.put()
                .uri("/company/projects/risks/internal/phone-response?callToken={callToken}&callSid={callSid}&status={status}", callToken, callSid, status)
                .header("X-Internal-Api-Key", internalApiKey)
                .retrieve()
                .toBodilessEntity();
    }

    public String getRiskCallToken(Long riskId, Long workerUserId) {
        return restClient.get()
                .uri("/company/projects/risks/internal/call-token?riskId={riskId}&workerUserId={workerUserId}", riskId, workerUserId)
                .header("X-Internal-Api-Key", internalApiKey)
                .retrieve()
                .body(String.class);
    }

}
