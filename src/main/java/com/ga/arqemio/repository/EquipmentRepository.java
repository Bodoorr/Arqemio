package com.ga.arqemio.repository;

import com.ga.arqemio.model.Equipment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE) //tells the JPA to request a database write lock on the equipment row
    @Query("SELECT e FROM Equipment e WHERE e.id = :equipmentId")
    Optional<Equipment> findByIdForUpdate(@Param("equipmentId") Long equipmentId);

    List<Equipment> findByCompanyMembershipsUserIdAndCompanyMembershipsStatus(
            Long userId,
            String status
    );
}
