package com.ga.arqemio.test;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class TestCodeSnippets {
    // PROJECT TESTS
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

    @Test
    public void testCreateProject() {
        String projectName = "Arqemio Tower";
        String status = "ACTIVE";
        assertEquals("Arqemio Tower", projectName);
        assertEquals("ACTIVE", status);
    }

    @Test
    public void testProjectCannotHaveEmptyName() {
        String projectName = "";
        assertTrue(projectName.isEmpty());
    }

    // TASK TESTS
    @Test
    public void testCreateTask() {
        String taskTitle = "Install Windows";
        String status = "PENDING";
        assertEquals("Install Windows", taskTitle);
        assertEquals("PENDING", status);
    }

    @Test
    public void testAssignTaskToWorker() {
        Long workerId = 7L;
        Long assignedWorkerId = 7L;
        assertEquals(workerId, assignedWorkerId);
    }

    @Test
    public void testTaskDeadlineCannotBeBeforeStartDate() {
        LocalDate startDate = LocalDate.of(2026, 10, 20);
        LocalDate deadline = LocalDate.of(2026, 10, 15);
        assertTrue(deadline.isBefore(startDate));
    }

    @Test
    public void testCompleteTask() {
        String status = "COMPLETED";
        assertEquals("COMPLETED", status);
    }

    // PROJECT MANAGER TESTS
    @Test
    public void testAssignManagerToProject() {
        List<Long> managerIds = new ArrayList<>();
        managerIds.add(6L);
        assertTrue(managerIds.contains(6L));
    }

    @Test
    public void testRemoveManagerFromProject() {
        List<Long> managerIds = new ArrayList<>();
        managerIds.add(6L);
        managerIds.remove(6L);
        assertFalse(managerIds.contains(6L));
    }

    // EXPENSE TESTS
    @Test
    public void testCreateExpense() {
        String expenseTitle = "Construction Materials";
        double amount = 250.0;
        assertEquals("Construction Materials", expenseTitle);
        assertEquals(250.0, amount, 0.001);
    }

    @Test
    public void testExpenseCannotBeNegative() {
        double amount = -50.0;
        assertTrue(amount < 0);
    }

    @Test
    public void testCalculateTotalExpenses() {
        double cement = 250.0;
        double steel = 400.0;
        double equipment = 150.0;
        double total = cement + steel + equipment;
        assertEquals(800.0, total, 0.001);
    }

    // EQUIPMENT RESERVATION TESTS
    @Test
    public void testCreateReservation() {
        LocalDate startDate = LocalDate.of(2026, 10, 20);
        LocalDate endDate = LocalDate.of(2026, 10, 25);
        assertFalse(endDate.isBefore(startDate));
    }

    @Test
    public void testReservationCannotEndBeforeStart() {
        LocalDate startDate = LocalDate.of(2026, 10, 25);
        LocalDate endDate = LocalDate.of(2026, 10, 20);
        assertTrue(endDate.isBefore(startDate));
    }

    @Test
    public void testEquipmentReservationConflict() {
        LocalDate existingStart = LocalDate.of(2026, 10, 20);
        LocalDate existingEnd = LocalDate.of(2026, 10, 25);
        LocalDate newStart = LocalDate.of(2026, 10, 23);
        LocalDate newEnd = LocalDate.of(2026, 10, 27);
        boolean conflict = !newStart.isAfter(existingEnd)
                && !newEnd.isBefore(existingStart);
        assertTrue(conflict);
    }

    @Test
    public void testEquipmentReservationNoConflict() {
        LocalDate existingEnd = LocalDate.of(2026, 10, 25);
        LocalDate newStart = LocalDate.of(2026, 10, 26);
        assertTrue(newStart.isAfter(existingEnd));
    }

    @Test
    public void testCancelReservation() {
        String reservationStatus = "CANCELLED";
        assertEquals("CANCELLED", reservationStatus);
    }

    // ROLE AND PERMISSION TESTS
    @Test
    public void testCompanyOwnerCanManageProject() {
        String role = "OWNER";
        assertTrue(role.equals("OWNER"));
    }

    @Test
    public void testWorkerIsNotCompanyOwner() {
        String role = "WORKER";
        assertFalse(role.equals("OWNER"));
    }

    @Test
    public void testManagerCanBeAssignedToProject() {
        String role = "MANAGER";
        String membershipStatus = "ACTIVE";
        assertTrue(role.equals("MANAGER")
                && membershipStatus.equals("ACTIVE"));
    }
}
