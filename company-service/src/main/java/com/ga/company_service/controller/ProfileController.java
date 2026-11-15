package com.ga.company_service.controller;

import com.ga.company_service.model.response.UserProfileResponse;
import com.ga.company_service.service.CompanyService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class ProfileController {
    private CompanyService companyService;

    @GetMapping("/companies/{companyId}/employees")
    public ResponseEntity<List<UserProfileResponse>> getEmployeeProfiles(@PathVariable Long companyId){
        return ResponseEntity.ok(companyService.getEmployeeProfiles(companyId));
    }

    @PatchMapping(path = "/companies/{companyId}/employees/{employeeId}/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileResponse> updateEmployeeProfile(
            @PathVariable Long companyId,
            @PathVariable Long employeeId,
            @RequestParam String name,
            @RequestParam String mobileNumber,
            @RequestParam String email,
            @RequestParam(required = false)MultipartFile profilePicture) throws IOException {
        return ResponseEntity.ok(companyService.updateEmployeeProfile(companyId,employeeId,name,mobileNumber,email,profilePicture));
    }
}
