package com.ga.arqemio.repository;

import com.ga.arqemio.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense,Long> {
    List<Expense> findByProjectCompanyMembershipsUserIdAndProjectCompanyMembershipsStatus(
            Long userId,
            String status
    );
}
