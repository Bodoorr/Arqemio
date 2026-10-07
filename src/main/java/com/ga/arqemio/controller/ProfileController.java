package com.ga.arqemio.controller;

import com.ga.arqemio.model.User;
import com.ga.arqemio.model.request.UpdateProfileRequest;
import com.ga.arqemio.model.response.ChangePasswordRequest;
import com.ga.arqemio.model.response.UserProfileResponse;
import com.ga.arqemio.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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
    public ResponseEntity<UserProfileResponse> updateProfile(@RequestBody UpdateProfileRequest updateProfileRequest){
        return ResponseEntity.ok(userService.updateProfile(updateProfileRequest));
    }

    @GetMapping("/companies/{companyId}/employees")
    public ResponseEntity<List<UserProfileResponse>> getEmployeeProfiles(@PathVariable Long companyId){
        return ResponseEntity.ok(userService.getEmployeeProfiles(companyId));
    }

    @PatchMapping(path = "/companies/{companyId}/employees/{employeeId}/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileResponse> updateEmployeeProfile(@PathVariable Long companyId, @PathVariable Long employeeId, @RequestParam String name, @RequestParam String mobileNumber, @RequestParam String email, @RequestParam(required = false)MultipartFile profilePicture) throws IOException {
        return ResponseEntity.ok(userService.updateEmployeeProfile(companyId, employeeId, name, mobileNumber, email, profilePicture));
    }

    @PatchMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest){
        return ResponseEntity.ok(userService.changePassword(changePasswordRequest));
    }
}
