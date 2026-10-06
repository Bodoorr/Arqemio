package com.ga.arqemio.model.request;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ExpenseRequest {
    private Long projectId;
    private String title;
    private double amount;
    private String category;
    private String description;
    private LocalDateTime expenseDateTime;
}
