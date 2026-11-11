package com.ga.company_service.service;

import com.ga.company_service.client.AuthClient;
import com.ga.company_service.model.Company;
import com.ga.company_service.model.CompanyMembership;
import com.ga.company_service.model.request.CompanyRequest;
import com.ga.company_service.model.response.UserMembershipResponse;
import com.ga.company_service.model.response.UserProfileResponse;
import com.ga.company_service.model.response.UserResponse;
import com.ga.company_service.repository.CompanyMembershipRepository;
import com.ga.company_service.repository.CompanyRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@AllArgsConstructor
public class CompanyService {
    private CompanyRepository companyRepository;
//    private AuditLogService auditLogService;
    private CompanyMembershipRepository companyMembershipRepository;
    private AuthClient authClient;

    private static final Logger logger= Logger.getLogger(CompanyService.class.getName());
    public UserResponse getCurrentLoggedInUser(){
        return (UserResponse) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public Company createCompany(CompanyRequest companyRequest){
        UserResponse currentUser= getCurrentLoggedInUser();
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
//        auditLogService.createAuditLog(currentUser,savedCompany,"CREATE_COMPANY","COMPANY", savedCompany.getId(), "Created company: "+ savedCompany.getName());
        logger.log(Level.INFO,"Company {0} created successfully by platform admin {1}", new Object[]{savedCompany.getId(), currentUser.getId()});
        return savedCompany;
    }

    public List<Company> getAllCompanies(){
        UserResponse currentUser= getCurrentLoggedInUser();
        if (!currentUser.getIsPlatformAdmin()){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only platform Admin can view all companies.");
        }
        return companyRepository.findAll();
    }

    public Company getCompanyById(Long id){
        UserResponse currentUser= getCurrentLoggedInUser();
        Company company= companyRepository.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Company not found"));

        boolean isPlatformAdmin= Boolean.TRUE.equals(currentUser.getIsPlatformAdmin());
        boolean isActiveMember= companyMembershipRepository.existsByUserIdAndCompanyIdAndStatus(currentUser.getId(), id, "ACTIVE");

        if (!isPlatformAdmin && !isActiveMember){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to view this company.");
        }
        return company;
    }

    public List<UserMembershipResponse> getUserMemberships(Long userId){
        UserResponse currentUser= getCurrentLoggedInUser();
        if (!currentUser.getId().equals(userId)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view your own memberships.");
        }

        List<CompanyMembership> memberships= companyMembershipRepository.findByUserIdAndStatus(userId, "ACTIVE");
        List<UserMembershipResponse> responses= new ArrayList<>();
        for (CompanyMembership membership: memberships){
            responses.add(new UserMembershipResponse(
                    membership.getId(),
                    membership.getCompany().getId(),
                    membership.getCompany().getName(),
                    membership.getRole(),
                    membership.getStatus()
            ));
        }
        return responses;
    }

    public List<UserProfileResponse> getEmployeeProfiles(Long companyId){
        UserResponse currentUser= getCurrentLoggedInUser();

        boolean isOwner= companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(currentUser.getId(), companyId, "OWNER","ACTIVE");
        if (!isOwner){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to view employees in this company.");
        }

        List<CompanyMembership> memberships= companyMembershipRepository.findByCompanyIdAndStatus(companyId, "ACTIVE");
        List<UserProfileResponse> employeeProfiles= new ArrayList<>();

        for (CompanyMembership membership: memberships){
            UserProfileResponse employee= authClient.getUserById(membership.getUserId());

            UserMembershipResponse membershipResponse= new UserMembershipResponse(
                    membership.getId(),
                    membership.getCompany().getId(),
                    membership.getCompany().getName(),
                    membership.getRole(),
                    membership.getStatus()
            );

            UserProfileResponse response= new UserProfileResponse(
                    employee.getId(),
                    employee.getName(),
                    employee.getEmail(),
                    employee.getMobileNumber(),
                    employee.getProfilePicture(),
                    List.of(membershipResponse),
                    employee.getIsCustomer()
            );
            employeeProfiles.add(response);
        }
        return employeeProfiles;
    }

}
