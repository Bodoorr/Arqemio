package com.ga.arqemio.controller;

import com.ga.arqemio.model.User;
import com.ga.arqemio.model.request.UpdateProfileRequest;
import com.ga.arqemio.model.response.UserProfileResponse;
import com.ga.arqemio.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class ProfileController {
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(){
        return ResponseEntity.ok(userService.getProfile());
    }

    @PatchMapping("/profile")
    public ResponseEntity<User> updateProfile(@RequestBody UpdateProfileRequest updateProfileRequest){
        return ResponseEntity.ok(userService.updateProfile(updateProfileRequest));
    }
}
