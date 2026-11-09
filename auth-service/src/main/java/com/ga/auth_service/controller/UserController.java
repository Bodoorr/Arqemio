package com.ga.auth_service.controller;

import com.ga.auth_service.model.User;
import com.ga.auth_service.model.request.ForgetPasswordRequest;
import com.ga.auth_service.model.request.LoginRequest;
import com.ga.auth_service.model.request.ResetPasswordRequest;
import com.ga.auth_service.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/auth/users")
public class UserController {
    private UserService userService;

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
    public ResponseEntity<User> getUserById(@PathVariable Long id){
        return ResponseEntity.ok(userService.getUserById(id));
    }


}
