package com.ga.arqemio.controller;

import com.ga.arqemio.model.User;
import com.ga.arqemio.security.MyUserDetails;
import com.ga.arqemio.service.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/notifications")
@AllArgsConstructor
public class NotificationController {
    private NotificationService notificationService;

    public static User getCurrentLoggedInUser(){
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    @GetMapping("/subscribe")
    public SseEmitter subscribe(){
        User currentUser= getCurrentLoggedInUser();
        return notificationService.subscribe(currentUser.getId());
    }

}
