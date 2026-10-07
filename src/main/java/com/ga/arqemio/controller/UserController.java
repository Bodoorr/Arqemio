package com.ga.arqemio.controller;

import com.ga.arqemio.model.request.ForgetPasswordRequest;
import com.ga.arqemio.model.request.LoginRequest;
import com.ga.arqemio.model.request.ResetPasswordRequest;
import com.ga.arqemio.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/auth/users")
public class UserController {
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest){
        System.out.println("Calling loginUser ==>");
        return userService.loginUser(loginRequest);
    }

    @PostMapping("/forget-password")
    public ResponseEntity<String> forgetPassword(@RequestBody ForgetPasswordRequest forgetPasswordRequest){
        return ResponseEntity.ok(userService.forgetPassword(forgetPasswordRequest));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest){
        return ResponseEntity.ok(userService.resetPassword(resetPasswordRequest));
    }


}
