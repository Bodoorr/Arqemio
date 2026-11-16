package com.ga.notification_service.controller;

import com.ga.notification_service.model.response.UserResponse;
import com.ga.notification_service.service.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/notifications")
@AllArgsConstructor
public class NotificationController {
    private NotificationService notificationService;

    public static UserResponse getCurrentLoggedInUser(){
        return (UserResponse) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @GetMapping("/subscribe")
    public SseEmitter subscribe(){
        UserResponse currentUser= getCurrentLoggedInUser();
        return notificationService.subscribe(currentUser.getId());
    }

    @PostMapping("/send")
    public ResponseEntity<Boolean> sendNotification(@RequestParam Long userId, @RequestParam String message){
        notificationService.sendNotification(userId, message);
        return ResponseEntity.ok(true);
    }

}
