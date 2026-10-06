package com.ga.arqemio.repository;

import com.ga.arqemio.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByCompanyMembershipsUserIdAndCompanyMembershipsStatus(
            Long userId,
            String status
    );
}
