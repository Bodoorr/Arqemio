package com.ga.arqemio.repository;

import com.ga.arqemio.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task,Long> {
    List<Task> findByProjectCompanyMembershipsUserIdAndProjectCompanyMembershipsStatus(
            Long userId,
            String status
    );
}
