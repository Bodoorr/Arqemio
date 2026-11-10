package com.ga.company_service.client;

import com.ga.company_service.model.response.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AuthClient {
    private RestClient restClient;

    public AuthClient(RestClient.Builder builder, @Value("${auth.service.url}") String authServiceUrl){
        this.restClient= builder.baseUrl(authServiceUrl).build();
    }

    public String getUserById(Long userId, String token){
        return restClient.get().uri("/auth/users/{id}", userId)
                //.header("Authorization", "Bearer " + token) same as this
                .headers(headers->headers.setBearerAuth(token))
                .retrieve()
                .body(String.class);
    }

    public UserResponse getCurrentUser(String token){
        return restClient.get()
                .uri("/users/current-user")
                .headers(headers->headers.setBearerAuth(token))
                .retrieve()
                .body(UserResponse.class);
    }

}
