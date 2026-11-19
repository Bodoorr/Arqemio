package com.ga.notification_service.client;

import com.ga.notification_service.model.response.UserProfileResponse;
import com.ga.notification_service.model.response.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class AuthClient {
    private RestClient restClient;

    @Value("${internal.api-key}")
    private String internalApiKey;

    public AuthClient(@Value("${auth.service.url}") String authServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(authServiceUrl)
                .build();
    }

    public UserResponse getCurrentUser(String token) {
        return restClient.get()
                .uri("/auth/users/current-user")
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .body(UserResponse.class);
    }

    public UserProfileResponse getUserById(Long userId) {
        return restClient.get()
                .uri("/auth/users/{id}", userId)
                .header("X-Internal-Api-Key", internalApiKey)
                .retrieve()
                .body(UserProfileResponse.class);
    }

    public Boolean sendEmail(String email, String subject, String message){
        Map<String, String> emailDetails= Map.of(
                "recipient", email, "subject", subject, "msgBody", message
        );
        Boolean sent= restClient.post()
                .uri("/auth/users/internal/send-email")
                .header("X-Internal-Api-Key", internalApiKey)
                .body(emailDetails)
                .retrieve()
                .body(Boolean.class);

        if (!Boolean.TRUE.equals(sent)){
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Auth service failed to send email.");
        }
        return sent;
    }
}
