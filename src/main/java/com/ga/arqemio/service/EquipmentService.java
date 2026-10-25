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
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@AllArgsConstructor
public class EquipmentService {
    private EquipmentRepository equipmentRepository;
    private CompanyRepository companyRepository;
    private CompanyMembershipRepository companyMembershipRepository;
    private static final Logger logger= Logger.getLogger(EquipmentService.class.getName());
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

        Equipment savedEquipment= equipmentRepository.save(equipment);
        logger.log(Level.INFO,"Equipment {0} created for company {1} by user {2}", new Object[]{savedEquipment.getId(), company.getId(), currentUser.getId()});
        return savedEquipment;
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
            logger.log(Level.WARNING,"User {0} attempted to view equipment {1} without permission", new Object[]{currentUser.getId(), equipmentId});
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

        Equipment savedEquipment= equipmentRepository.save(equipment);
        logger.log(Level.INFO,"Equipment {0} updated by user {1}", new Object[]{savedEquipment.getId(), currentUser.getId()});
        return savedEquipment;
    }

    public Equipment archiveEquipment(Long equipmentId){
        User currentUser= getCurrentLoggedInUser();
        Equipment equipment= equipmentRepository.findById(equipmentId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment not found."));
        validateEquipmentManagementAccess(currentUser, equipment.getCompany());
        equipment.setStatus("ARCHIVED");

        Equipment savedEquipment= equipmentRepository.save(equipment);
        logger.log(Level.INFO,"Equipment {0} archived by user {1}", new Object[]{savedEquipment.getId(), currentUser.getId()});
        return savedEquipment;
    }

    private void validateEquipmentManagementAccess(User user, Company company){
        boolean isCompanyOwner= companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(user.getId(), company.getId(), "OWNER", "ACTIVE");
        if (!isCompanyOwner){
            logger.log(Level.WARNING,"User {0} attempted to manage equipment for company {1} without permission", new Object[]{user.getId(), company.getId()});
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to manage equipment for this company.");
        }
    }

    private void validateEquipmentQuantity(EquipmentRequest equipmentRequest){
        if (equipmentRequest.getQuantity()<0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Equipment quantity cannot be negative.");
        }
    }

}
