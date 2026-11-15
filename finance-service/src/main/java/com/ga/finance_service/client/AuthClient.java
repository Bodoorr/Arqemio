package com.ga.finance_service.client;

import com.ga.finance_service.model.response.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

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


    public UserProfileResponse getUserById(Long userId, String internalApiKey){
        return restClient.get()
                .uri("/auth/users/{id}", userId)
                .header("X-Internal-Api-Key", internalApiKey)
                .retrieve()
                .body(UserProfileResponse.class);
    }

}
