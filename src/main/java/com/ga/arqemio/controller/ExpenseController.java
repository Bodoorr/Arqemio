package com.ga.arqemio.controller;

import com.ga.arqemio.model.Expense;
import com.ga.arqemio.model.request.ExpenseRequest;
import com.ga.arqemio.model.response.ExpenseResponse;
import com.ga.arqemio.service.ExpenseService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/company/projects/expenses")
@AllArgsConstructor
public class ExpenseController {
    private ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(@RequestBody ExpenseRequest expenseRequest){
        Expense expense= expenseService.createExpense(expenseRequest);
        ExpenseResponse expenseResponse=new ExpenseResponse(
                expense.getId(),
                expense.getProject().getId(),
                expense.getProject().getName(),
                expense.getMember().getId(),
                expense.getMember().getUser().getName(),
                expense.getTitle(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getDescription(),
                expense.getStatus(),
                expense.getExpenseDateTime()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseResponse);
    }
}
