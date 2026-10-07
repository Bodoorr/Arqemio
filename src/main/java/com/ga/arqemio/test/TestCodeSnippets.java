package com.ga.arqemio.test;
import com.ga.arqemio.repository.CompanyMembershipRepository;
import com.ga.arqemio.repository.CompanyRepository;
import com.ga.arqemio.repository.ProjectRepository;
import com.ga.arqemio.service.AuditLogService;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class TestCodeSnippets {
    @Test
    public void testProjectEndDateIsAfterStartDate() {
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate expectedEndDate = LocalDate.of(2026, 12, 1);
        assertFalse(expectedEndDate.isBefore(startDate));
    }

    @Test
    public void testProjectEndDateCannotBeBeforeStartDate() {
        LocalDate startDate = LocalDate.of(2026, 10, 10);
        LocalDate expectedEndDate = LocalDate.of(2026, 10, 5);
        assertTrue(expectedEndDate.isBefore(startDate));
    }
}
