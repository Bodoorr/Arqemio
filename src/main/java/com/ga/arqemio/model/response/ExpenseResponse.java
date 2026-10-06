package com.ga.arqemio.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ExpenseResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private Long recordedByMembershipId;
    private String recordedByName;
    private String title;
    private double amount;
    private String category;
    private String description;
    private String status;
    private LocalDateTime expenseDateTime;

}
