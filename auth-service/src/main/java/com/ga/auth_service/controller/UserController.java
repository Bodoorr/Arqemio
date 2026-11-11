package com.ga.auth_service.controller;

import com.ga.auth_service.model.EmailDetails;
import com.ga.auth_service.model.User;
import com.ga.auth_service.model.request.ForgetPasswordRequest;
import com.ga.auth_service.model.request.LoginRequest;
import com.ga.auth_service.model.request.RegistrationRequest;
import com.ga.auth_service.model.request.ResetPasswordRequest;
import com.ga.auth_service.model.response.UserProfileResponse;
import com.ga.auth_service.model.response.UserResponse;
import com.ga.auth_service.service.EmailService;
import com.ga.auth_service.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/auth/users")
public class UserController {
    private UserService userService;
    private EmailService emailService;

    @Value("${internal.api-key}")
    private String internalApiKey;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest){
//        System.out.println("Calling loginUser ==>");
        return userService.loginUser(loginRequest);
    }

    @PostMapping("/forget-password")
    public ResponseEntity<String> forgetPassword(@Valid @RequestBody ForgetPasswordRequest forgetPasswordRequest){
        return ResponseEntity.ok(userService.forgetPassword(forgetPasswordRequest));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest){
        return ResponseEntity.ok(userService.resetPassword(resetPasswordRequest));
    }

    //connect to Auth service
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getUserById(@PathVariable Long id, @RequestHeader("X-Internal-Api-Key") String apiKey){
        if (internalApiKey.isBlank() || !internalApiKey.equals(apiKey)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API key");
        }
        User user= userService.getUserById(id);

        UserProfileResponse response= new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getProfilePicture(),
                null,
                user.getIsCustomer()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current-user")
    public ResponseEntity<UserResponse> getCurrentUser(){
        User user= userService.getCurrentLoggedInUser();

        UserResponse response= new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getIsPlatformAdmin()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestHeader("X-Internal-Api-Key") String apiKey, @Valid @RequestBody RegistrationRequest request){

        if (internalApiKey.isBlank() || !internalApiKey.equals(apiKey)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid API key");
        }

        User user= new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setProfilePicture(request.getProfilePicture());
        user.setStatus(true);
        user.setIsPlatformAdmin(false);

        User savedUser= userService.createUser(user);

        UserResponse response= new UserResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getIsPlatformAdmin()
                );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/internal/send-email")
    public ResponseEntity<Boolean> sendEmail(@RequestHeader("X-Internal-Api-Key") String apiKey,  @RequestBody EmailDetails emailDetails) {
        if (internalApiKey.isBlank() || !internalApiKey.equals(apiKey)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid API key");
        }
        return ResponseEntity.ok(emailService.sendHtmlMail(emailDetails));
    }


}
