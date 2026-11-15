package com.ga.finance_service.client;

import com.ga.finance_service.model.response.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AuthClient {
    private RestClient restClient;

    public AuthClient(@Value("${auth.service.url}") String authServiceUrl){
        this.restClient=RestClient.builder().baseUrl(authServiceUrl).build();
    }

    public UserResponse getCurrentUser(String token){
        return restClient.get()
                .uri("/auth/users/current-user")
                .headers(headers->headers.setBearerAuth(token))
                .retrieve()
                .body(UserResponse.class);
    }
}
