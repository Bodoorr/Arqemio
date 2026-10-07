package com.ga.arqemio.repository;

import com.ga.arqemio.model.ProjectShareLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectShareLinkRepository extends JpaRepository<ProjectShareLink, Long> {
    Optional<ProjectShareLink> findByToken(String token);
}
