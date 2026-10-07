package com.ga.arqemio.repository;

import com.ga.arqemio.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByCompanyMembershipsUserIdAndCompanyMembershipsStatus(
            Long userId,
            String status
    );

    Page<Project> findByCompanyMembershipsUserIdAndCompanyMembershipsStatus(
            Long userId,
            String status,
            Pageable pageable
    );

    boolean existsByIdAndManagersUserIdAndManagersStatus(
            Long projectId,
            Long userId,
            String status
    );

    boolean existsByIdAndWorkersId(
            Long projectId,
            Long membershipId
    );

    boolean existsByIdAndWorkersUserIdAndWorkersStatus(
            Long projectId,
            Long userId,
            String status
    );


}
