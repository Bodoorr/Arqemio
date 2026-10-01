package com.ga.arqemio.repository;

import com.ga.arqemio.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation,Long> {
Optional<Invitation> findByToken(String token);
boolean existsByEmailIgnoreCaseAndRoleAndStatus(String email, String role, String status);

}
