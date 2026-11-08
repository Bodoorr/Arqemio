package com.ga.arqemio.repository;

import com.ga.arqemio.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByEquipmentId(Long equipmentId);
    List<Reservation> findByProjectId(Long projectId);
}
