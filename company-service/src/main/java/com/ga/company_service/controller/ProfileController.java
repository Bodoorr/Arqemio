package com.ga.company_service.controller;

import com.ga.company_service.model.response.UserProfileResponse;
import com.ga.company_service.service.CompanyService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
