package com.ga.arqemio.repository;

import com.ga.arqemio.model.ProjectUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectUpdateRepository extends JpaRepository<ProjectUpdate, Long> {
    List<ProjectUpdate> findByProjectId(Long projectId);
}
