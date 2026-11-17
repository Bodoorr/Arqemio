package com.ga.notification_service.controller;

import com.ga.notification_service.model.response.UserResponse;
import com.ga.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    @Value("${internal.api-key}")
    private String internalApiKey;

    private final NotificationService notificationService;

    public static UserResponse getCurrentLoggedInUser(){
        return (UserResponse) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @GetMapping("/subscribe")
    public SseEmitter subscribe(){
        UserResponse currentUser= getCurrentLoggedInUser();
        return notificationService.subscribe(currentUser.getId());
    }

    @PostMapping("/send")
    public ResponseEntity<Boolean> sendNotification(@RequestHeader("X-Internal-Api-Key") String apiKey, @RequestParam Long userId, @RequestParam String message){
        if (internalApiKey.isBlank() || !internalApiKey.equals(apiKey)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API key");
        }

        notificationService.sendNotification(userId, message);
        return ResponseEntity.ok(true);
    }

}
