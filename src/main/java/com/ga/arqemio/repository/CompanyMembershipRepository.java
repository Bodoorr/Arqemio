package com.ga.arqemio.repository;

import com.ga.arqemio.model.CompanyMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyMembershipRepository extends JpaRepository<CompanyMembership, Long> {
    boolean existsByUserIdAndCompanyIdAndRoleAndStatus(
            Long userId,
            Long companyId,
            String role,
            String status
    );

    boolean existsByUserIdAndCompanyIdAndStatus(
            Long userId,
            Long companyId,
            String status
    );

}
