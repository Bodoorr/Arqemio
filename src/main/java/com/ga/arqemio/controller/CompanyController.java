package com.ga.arqemio.controller;

import com.ga.arqemio.model.Company;
import com.ga.arqemio.model.request.CompanyRequest;
import com.ga.arqemio.model.response.CompanyResponse;
import com.ga.arqemio.service.CompanyService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/comapny")
@AllArgsConstructor
public class CompanyController {
    private CompanyService companyService;

    @PostMapping
    public ResponseEntity<CompanyResponse> creatCompany(@RequestBody CompanyRequest companyRequest){
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
}
