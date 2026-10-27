package com.ga.arqemio.service;

import com.ga.arqemio.model.Company;
import com.ga.arqemio.model.User;
import com.ga.arqemio.model.request.CompanyRequest;
import com.ga.arqemio.repository.CompanyRepository;
import com.ga.arqemio.security.MyUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@AllArgsConstructor
public class CompanyService {
    private CompanyRepository companyRepository;
    private AuditLogService auditLogService;
    private static final Logger logger= Logger.getLogger(CompanyService.class.getName());
    public User getCurrentLoggedInUser(){
        MyUserDetails userDetails= (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    public Company createCompany(CompanyRequest companyRequest){
        User currentUser= getCurrentLoggedInUser();
        if (!currentUser.getIsPlatformAdmin()){
            logger.log(Level.WARNING, "User {0} attempted to create a company without platform admin permission", currentUser.getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Platform Admin can create companies.");
        }

        Company company=new Company();
        company.setName(companyRequest.getName());
        company.setDescription(companyRequest.getDescription());
        company.setLocation(companyRequest.getLocation());
        company.setAddress(companyRequest.getAddress());
        company.setStatus("ACTIVE");
        company.setOwner(null);

        Company savedCompany= companyRepository.save(company);
        auditLogService.createAuditLog(currentUser,savedCompany,"CREATE_COMPANY","COMPANY", savedCompany.getId(), "Created company: "+ savedCompany);
        logger.log(Level.INFO,"Company {0} created successfully by platform admin {1}", new Object[]{savedCompany.getId(), currentUser.getId()});
        return savedCompany;
    }
}
