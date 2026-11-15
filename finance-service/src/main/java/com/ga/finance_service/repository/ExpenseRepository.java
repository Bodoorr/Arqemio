package com.ga.finance_service.repository;

import com.ga.finance_service.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense,Long> {
    List<Expense> findByProjectId(Long projectId);
}
