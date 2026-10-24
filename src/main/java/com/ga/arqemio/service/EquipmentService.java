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

import java.util.List;

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
        validateEquipmentManagementAccess(currentUser, company);
        validateEquipmentQuantity(equipmentRequest);

        Equipment equipment=new Equipment();
        equipment.setCompany(company);
        equipment.setName(equipmentRequest.getName());
        equipment.setDescription(equipmentRequest.getDescription());
        equipment.setImageUrl(equipmentRequest.getImageUrl());
        equipment.setStatus(equipmentRequest.getStatus());
        equipment.setQuantity(equipmentRequest.getQuantity());

        return equipmentRepository.save(equipment);
    }

    public List<Equipment> getAllEquipment(){
        User currentUser= getCurrentLoggedInUser();
        return equipmentRepository.findByCompanyMembershipsUserIdAndCompanyMembershipsStatus(currentUser.getId(), "ACTIVE");
    }

    public Equipment getEquipmentById(Long equipmentId){
        User currentUser= getCurrentLoggedInUser();
        Equipment equipment= equipmentRepository.findById(equipmentId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment not found."));

        boolean isActiveMember= companyMembershipRepository.existsByUserIdAndCompanyIdAndStatus(currentUser.getId(), equipment.getCompany().getId(), "ACTIVE");
        if (!isActiveMember){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You're not allowed to view this equipment.");
        }
        return equipment;
    }

    public Equipment updateEquipment(Long equipmentId, EquipmentRequest equipmentRequest){
        User currentUser= getCurrentLoggedInUser();
        Equipment equipment= equipmentRepository.findById(equipmentId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment not found."));
        validateEquipmentManagementAccess(currentUser, equipment.getCompany());
        validateEquipmentQuantity(equipmentRequest);

        equipment.setName(equipmentRequest.getName());
        equipment.setDescription(equipmentRequest.getDescription());
        equipment.setImageUrl(equipmentRequest.getImageUrl());
        equipment.setStatus(equipmentRequest.getStatus());
        equipment.setQuantity(equipmentRequest.getQuantity());

        return equipmentRepository.save(equipment);
    }

    public Equipment archiveEquipment(Long equipmentId){
        User currentUser= getCurrentLoggedInUser();
        Equipment equipment= equipmentRepository.findById(equipmentId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment not found."));
        validateEquipmentManagementAccess(currentUser, equipment.getCompany());
        equipment.setStatus("ARCHIVED");

        return equipmentRepository.save(equipment);
    }

    private void validateEquipmentManagementAccess(User user, Company company){
        boolean isCompanyOwner= companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(user.getId(), company.getId(), "OWNER", "ACTIVE");
        if (!isCompanyOwner){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to manage equipment for this company.");
        }
    }

    private void validateEquipmentQuantity(EquipmentRequest equipmentRequest){
        if (equipmentRequest.getQuantity()<0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Equipment quantity cannot be negative.");
        }
    }

}
