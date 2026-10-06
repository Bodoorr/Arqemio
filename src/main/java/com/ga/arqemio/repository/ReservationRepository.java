package com.ga.arqemio.repository;

import com.ga.arqemio.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByProjectCompanyMembershipsUserIdAndProjectCompanyMembershipsStatus(
            Long userId,
            String status
    );

    List<Reservation> findByEquipmentIdAndStatus(
            Long equipmentId,
            String status
    );

}
