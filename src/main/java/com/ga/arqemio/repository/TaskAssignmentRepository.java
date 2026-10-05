package com.ga.arqemio.repository;

import com.ga.arqemio.model.Invitation;
import com.ga.arqemio.model.Task;
import com.ga.arqemio.model.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {
}
