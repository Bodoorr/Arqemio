package com.ga.arqemio.repository;

import com.ga.arqemio.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
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

    Page<Project> findByIdIn(List<Long> ids, Pageable pageable);

    Page<Project> findByIdInAndNameContainingIgnoreCase(
            List<Long> ids, String name, Pageable pageable);

    Page<Project> findByIdInAndStatus(
            List<Long> ids, String status, Pageable pageable);

    List<Project> findByCompanyId(Long companyId);

    List<Project> findByManagersId(Long membershipId);

    List<Project> findByWorkersId(Long membershipId);
}
