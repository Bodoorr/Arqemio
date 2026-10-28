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

import java.util.ArrayList;
import java.util.List;

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

    private CompanyResponse toResponse(Company company) {
        return new CompanyResponse(
                true,
                "Company retrieved successfully",
                company.getId(),
                company.getName(),
                company.getDescription(),
                company.getLocation(),
                company.getAddress(),
                company.getStatus()
        );
    }
}
