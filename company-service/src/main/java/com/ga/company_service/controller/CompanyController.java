package com.ga.company_service.controller;

import com.ga.company_service.client.AuthClient;
import com.ga.company_service.model.Company;
import com.ga.company_service.model.request.CompanyRequest;
import com.ga.company_service.model.response.CompanyResponse;
import com.ga.company_service.model.response.UserMembershipResponse;
import com.ga.company_service.model.response.UserProfileResponse;
import com.ga.company_service.service.CompanyService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/company")
@AllArgsConstructor
public class CompanyController {
    private CompanyService companyService;
    private AuthClient authClient;

    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CompanyRequest companyRequest){
        Company company= companyService.createCompany(companyRequest);
        CompanyResponse companyResponse= toResponse(company);
        companyResponse.setMessage("Company created successfully.");
        return ResponseEntity.status(HttpStatus.CREATED).body(companyResponse);
    }

    @GetMapping
    public ResponseEntity<List<CompanyResponse>> getAllCompanies() {
        List<Company> companies = companyService.getAllCompanies();
        List<CompanyResponse> companyResponses = new ArrayList<>();

        for (Company company : companies) {
            companyResponses.add(toResponse(company));
        }
        return ResponseEntity.ok(companyResponses);
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable Long companyId) {
        Company company = companyService.getCompanyById(companyId);
        return ResponseEntity.ok(toResponse(company));
    }

    @GetMapping("/users/{userId}/memberships")
    public ResponseEntity<List<UserMembershipResponse>> getUserMemberships(@PathVariable Long userId){
        return ResponseEntity.ok(companyService.getUserMemberships(userId));
    }

    private CompanyResponse toResponse(Company company) {
        Long ownerId= company.getOwner() != null ? company.getOwner().getUserId(): null;
        UserProfileResponse owner = ownerId != null ? authClient.getUserById(ownerId): null;
        return new CompanyResponse(
                true,
                "Company retrieved successfully",
                company.getId(),
                company.getName(),
                company.getDescription(),
                company.getLocation(),
                company.getAddress(),
                company.getStatus(),
                ownerId,
                owner != null ? owner.getName() : null,
                owner != null ? owner.getEmail() : null
        );
    }
}
