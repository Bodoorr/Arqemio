package com.ga.arqemio.controller;

import com.ga.arqemio.model.Equipment;
import com.ga.arqemio.model.request.EquipmentRequest;
import com.ga.arqemio.model.response.EquipmentResponse;
import com.ga.arqemio.service.EquipmentService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/company/equipments")
@AllArgsConstructor
public class EquipmentController {
    private EquipmentService equipmentService;

    @PostMapping
    public ResponseEntity<EquipmentResponse> createEquipment(@RequestBody EquipmentRequest equipmentRequest){
        Equipment equipment= equipmentService.createEquipment(equipmentRequest);
        EquipmentResponse equipmentResponse=new EquipmentResponse(
                equipment.getId(),
                equipment.getCompany().getId(),
                equipment.getCompany().getName(),
                equipment.getName(),
                equipment.getDescription(),
                equipment.getImageUrl(),
                equipment.getStatus(),
                equipment.getQuantity(),
                equipment.getCreatedAt(),
                equipment.getUpdatedAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(equipmentResponse);
    }

    @GetMapping
    public ResponseEntity<List<EquipmentResponse>> getAllEquipments(){
        List<Equipment> equipments= equipmentService.getAllEquipment();
        List<EquipmentResponse> equipmentResponses=new ArrayList<>();
        for (Equipment equipment: equipments){
            EquipmentResponse equipmentResponse= new EquipmentResponse(
                    equipment.getId(),
                    equipment.getCompany().getId(),
                    equipment.getCompany().getName(),
                    equipment.getName(),
                    equipment.getDescription(),
                    equipment.getImageUrl(),
                    equipment.getStatus(),
                    equipment.getQuantity(),
                    equipment.getCreatedAt(),
                    equipment.getUpdatedAt()
            );
            equipmentResponses.add(equipmentResponse);
        }
        return ResponseEntity.ok(equipmentResponses);
    }

    @GetMapping("/{equipmentId}")
    public ResponseEntity<EquipmentResponse> getEquipment(@PathVariable Long equipmentId){
        Equipment equipment= equipmentService.getEquipmentById(equipmentId);

        EquipmentResponse equipmentResponse=new EquipmentResponse(
                equipment.getId(),
                equipment.getCompany().getId(),
                equipment.getCompany().getName(),
                equipment.getName(),
                equipment.getDescription(),
                equipment.getImageUrl(),
                equipment.getStatus(),
                equipment.getQuantity(),
                equipment.getCreatedAt(),
                equipment.getUpdatedAt()
        );
        return ResponseEntity.ok(equipmentResponse);
    }

    @PutMapping("/{equipmentId}")
    public ResponseEntity<EquipmentResponse> updateEquipment(@PathVariable Long equipmentId, @RequestBody EquipmentRequest equipmentRequest){
        Equipment equipment= equipmentService.updateEquipment(equipmentId,equipmentRequest);

        EquipmentResponse equipmentResponse=new EquipmentResponse(
                equipment.getId(),
                equipment.getCompany().getId(),
                equipment.getCompany().getName(),
                equipment.getName(),
                equipment.getDescription(),
                equipment.getImageUrl(),
                equipment.getStatus(),
                equipment.getQuantity(),
                equipment.getCreatedAt(),
                equipment.getUpdatedAt()
        );
        return ResponseEntity.ok(equipmentResponse);
    }

    @DeleteMapping("/{equipmentId}")
    public ResponseEntity<EquipmentResponse> archiveEquipment(@PathVariable Long equipmentId){
        Equipment equipment= equipmentService.archiveEquipment(equipmentId);

        EquipmentResponse equipmentResponse=new EquipmentResponse(
                equipment.getId(),
                equipment.getCompany().getId(),
                equipment.getCompany().getName(),
                equipment.getName(),
                equipment.getDescription(),
                equipment.getImageUrl(),
                equipment.getStatus(),
                equipment.getQuantity(),
                equipment.getCreatedAt(),
                equipment.getUpdatedAt()
        );
        return ResponseEntity.ok(equipmentResponse);
    }
}
