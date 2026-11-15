package com.ga.finance_service.client;

import com.ga.finance_service.model.response.ProjectResponse;
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

    public Long validateExpenseAccess(Long projectId, Long userId){
        return restClient.get()
                .uri("/company/projects/{projectId}/internal/expense-access/{userId}", projectId, userId)
                .header("X-Internal-Api-Key", internalApiKey)
                .retrieve()
                .body(Long.class);
    }

    public Boolean validateExpenseOwnership(Long projectId, Long membershipId, Long userId){
        return restClient.get()
                .uri("/company/projects/{projectId}/internal/expense-ownership/{membershipId}/{userId}", projectId, membershipId,userId)
                .header("X-Internal-Api-Key", internalApiKey)
                .retrieve()
                .body(Boolean.class);
    }

    public ProjectResponse getProjectById(Long projectId, String token){
        return restClient.get()
                .uri("/company/projects/{projectId}", projectId)
                .headers(headers->headers.setBearerAuth(token))
                .retrieve()
                .body(ProjectResponse.class);
    }

    public ProjectResponse[] getAllProjects(String token){
        return restClient.get()
                .uri("/company/projects")
                .headers(headers->headers.setBearerAuth(token))
                .retrieve()
                .body(ProjectResponse[].class);
    }

}
