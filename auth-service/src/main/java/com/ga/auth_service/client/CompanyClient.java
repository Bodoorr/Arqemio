package com.ga.auth_service.client;

import com.ga.auth_service.model.response.UserMembershipResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;


@Service
public class CompanyClient {
    private RestClient restClient;

    public CompanyClient(RestClient.Builder builder, @Value("${company.service.url}") String companyServiceUrl){
        this.restClient= builder.baseUrl(companyServiceUrl).build();
    }

    public UserMembershipResponse[] getMembershipByUserId(Long userId, String token){
        return restClient.get()
                .uri("/company/users/{userId}/memberships",userId)
                .header("Authorization", token)
                .retrieve()
                .body(UserMembershipResponse[].class);
    }

}
