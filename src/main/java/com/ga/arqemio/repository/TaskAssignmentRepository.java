package com.ga.arqemio.repository;

import com.ga.arqemio.model.Invitation;
import com.ga.arqemio.model.Task;
import com.ga.arqemio.model.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {
    boolean existsByTaskIdAndAssignedToId(
            Long taskId,
            Long membershipId
    );

    Optional<TaskAssignment> findByTaskIdAndAssignedToId(
            Long taskId,
            Long membershipId
    );

    boolean existsByTaskIdAndAssignedToUserIdAndAssignedToStatus(
            Long taskId,
            Long userId,
            String status
    );
}
