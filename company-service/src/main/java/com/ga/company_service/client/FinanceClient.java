package com.ga.company_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class FinanceClient {
    private RestClient restClient;

    @Value("${internal.api-key}")
    private String internalApiKey;

    public FinanceClient(@Value("${finance.service.url}") String financeServiceUrl){
        this.restClient= RestClient.builder().baseUrl(financeServiceUrl).build();
    }

    public Long getExpenseProjectId(Long expenseId){
        return restClient.get()
                .uri("/company/projects/expenses/{expenseId}/internal/project", expenseId)
                .header("X-Internal-Api-Key", internalApiKey)
                .retrieve()
                .body(Long.class);
    }

    public Long getExpenseMemberId(Long expenseId) {
        return restClient.get()
                .uri("/company/projects/expenses/{expenseId}/internal/member", expenseId)
                .header("X-Internal-Api-Key", internalApiKey)
                .retrieve()
                .body(Long.class);
    }
}
