package com.ga.company_service.client;

import com.ga.company_service.model.EmailDetails;
import com.ga.company_service.model.request.CustomerRegistrationRequest;
import com.ga.company_service.model.request.RegistrationRequest;
import com.ga.company_service.model.response.UserProfileResponse;
import com.ga.company_service.model.response.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthClient {
    private RestClient restClient;

    @Value("${internal.api-key}")
    private String internalApiKey;

    public AuthClient( @Value("${auth.service.url}") String authServiceUrl){
        this.restClient= RestClient.builder()
                .baseUrl(authServiceUrl).build();
    }

    public UserProfileResponse getUserById(Long userId){
        return restClient.get().uri("/auth/users/{id}", userId)
                .header("X-Internal-Api-Key", internalApiKey)
                .retrieve()
                .body(UserProfileResponse.class);
    }

    public UserResponse getCurrentUser(String token){
        return restClient.get()
                .uri("/auth/users/current-user")
                .headers(headers->headers.setBearerAuth(token))
                .retrieve()
                .body(UserResponse.class);
    }

    public UserResponse registerUser(RegistrationRequest request){
        return restClient.post().uri("/auth/users/register")
                .header("X-Internal-Api-Key", internalApiKey)
                .body(request)
                .retrieve()
                .body(UserResponse.class);
    }

    public Boolean sendEmail(EmailDetails emailDetails){
        return restClient.post()
                .uri("/auth/users/internal/send-email")
                .header("X-Internal-Api-Key", internalApiKey)
                .body(emailDetails)
                .retrieve()
                .body(Boolean.class);
    }

    public Boolean existsByEmail(String email) {
        return restClient.get()
                .uri("/auth/users/exists?email={email}", email)
                .header("X-Internal-Api-Key", internalApiKey)
                .retrieve()
                .body(Boolean.class);
    }

    public UserResponse registerCustomer(CustomerRegistrationRequest customerRegistrationRequest){
        return restClient.post()
                .uri("/auth/users/internal/register-customer")
                .header("X-Internal-Api-Key", internalApiKey)
                .body(customerRegistrationRequest)
                .retrieve()
                .body(UserResponse.class);
    }

    public UserProfileResponse updateEmployee(Long employeeId, String name, String mobileNumber, String email, String profilePictureUrl){
        Map<String, String> request= new HashMap<>();
        request.put("name", name);
        request.put("mobileNumber", mobileNumber);
        request.put("email", email);
        request.put("profilePictureUrl", profilePictureUrl);

        return restClient.patch()
                .uri("/auth/users/internal/employees/{id}", employeeId)
                .header("X-Internal-Api-Key", internalApiKey)
                .body(request)
                .retrieve()
                .body(UserProfileResponse.class);
    }
}
