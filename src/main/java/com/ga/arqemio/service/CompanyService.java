package com.ga.arqemio.service;

import com.ga.arqemio.model.Company;
import com.ga.arqemio.model.User;
import com.ga.arqemio.model.request.CompanyRequest;
import com.ga.arqemio.repository.CompanyRepository;
import com.ga.arqemio.security.MyUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CompanyService {
    private CompanyRepository companyRepository;

    public User getCurrentLoggedInUser(){
        MyUserDetails userDetails= (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    public Company createCompany(CompanyRequest companyRequest){
        User currentUser= getCurrentLoggedInUser();

        if (!currentUser.getIsPlatformAdmin()){
            throw new RuntimeException("Only Platform Admin can create companies.");
        }

        Company company=new Company();
        company.setName(companyRequest.getName());
        company.setDescription(companyRequest.getDescription());
        company.setLocation(companyRequest.getLocation());
        company.setAddress(companyRequest.getAddress());
        company.setStatus("ACTIVE");
        company.setOwner(null);

        return companyRepository.save(company);
    }
}
