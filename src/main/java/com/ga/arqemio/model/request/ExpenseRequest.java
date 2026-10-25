package com.ga.arqemio.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ExpenseRequest {
    @NotNull(message = "Project ID is required.")
    private Long projectId;
    @NotBlank(message = "Expense title is required.")
    private String title;
    @Positive(message = "Expense amount must be greater than zero.")
    private double amount;
    @NotBlank(message = "Expense category is required.")
    private String category;
    private String description;
    @NotNull(message = "Expense date and time are required.")
    private LocalDateTime expenseDateTime;
}
