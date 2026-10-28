package com.ga.arqemio.controller;

import com.ga.arqemio.model.Company;
import com.ga.arqemio.model.request.CompanyRequest;
import com.ga.arqemio.model.response.CompanyResponse;
import com.ga.arqemio.service.CompanyService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/company")
@AllArgsConstructor
public class CompanyController {
    private CompanyService companyService;

    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CompanyRequest companyRequest){
        Company company= companyService.createCompany(companyRequest);

        CompanyResponse companyResponse= new CompanyResponse(
                true,
                "Company created successfully.",
                company.getId(),
                company.getName(),
                company.getDescription(),
                company.getLocation(),
                company.getAddress(),
                company.getStatus()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(companyResponse);
    }

    @GetMapping
    public ResponseEntity<?> getAllCompanies() {
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCompanyById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }
}
