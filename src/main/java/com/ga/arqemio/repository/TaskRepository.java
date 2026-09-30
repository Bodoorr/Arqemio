package com.ga.arqemio.repository;

import com.ga.arqemio.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task,Long> {
}
