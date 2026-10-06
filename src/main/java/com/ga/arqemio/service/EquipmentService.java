package com.ga.arqemio.service;

import com.ga.arqemio.model.Company;
import com.ga.arqemio.model.Equipment;
import com.ga.arqemio.model.User;
import com.ga.arqemio.model.request.EquipmentRequest;
import com.ga.arqemio.repository.CompanyMembershipRepository;
import com.ga.arqemio.repository.CompanyRepository;
import com.ga.arqemio.repository.EquipmentRepository;
import com.ga.arqemio.security.MyUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class EquipmentService {
    private EquipmentRepository equipmentRepository;
    private CompanyRepository companyRepository;
    private CompanyMembershipRepository companyMembershipRepository;

    public static User getCurrentLoggedInUser(){
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    public Equipment createEquipment(EquipmentRequest equipmentRequest){
        User currentUser= getCurrentLoggedInUser();
        Company company= companyRepository.findById(equipmentRequest.getCompanyId()).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found."));
        boolean isPlatformAdmin= currentUser.getIsPlatformAdmin().equals(true);
        boolean isCompanyOwner= companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(currentUser.getId(), equipmentRequest.getCompanyId(), "OWNER", "ACTIVE");

        if (!isPlatformAdmin && !isCompanyOwner){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to create Equipment for this company.");
        }

        if (equipmentRequest.getQuantity()<0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Equipment quantity cannot be negative.");
        }

        Equipment equipment=new Equipment();
        equipment.setCompany(company);
        equipment.setName(equipmentRequest.getName());
        equipment.setDescription(equipmentRequest.getDescription());
        equipment.setImageUrl(equipmentRequest.getImageUrl());
        equipment.setStatus(equipmentRequest.getStatus());
        equipment.setQuantity(equipmentRequest.getQuantity());

        return equipmentRepository.save(equipment);
    }
}
