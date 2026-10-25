package com.ga.arqemio.controller;

import com.ga.arqemio.model.Expense;
import com.ga.arqemio.model.request.ExpenseRequest;
import com.ga.arqemio.model.response.ExpenseResponse;
import com.ga.arqemio.service.ExpenseService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/company/projects/expenses")
@AllArgsConstructor
public class ExpenseController {
    private ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(@RequestBody ExpenseRequest expenseRequest){
        Expense expense= expenseService.createExpense(expenseRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(expense));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses(){
        List<Expense> expenses= expenseService.getAllExpenses();
        List<ExpenseResponse> expenseResponses= new ArrayList<>();

        for (Expense expense:expenses){
            expenseResponses.add(toResponse(expense));
        }
        return ResponseEntity.ok(expenseResponses);
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> getExpense(@PathVariable Long expenseId){
        Expense expense= expenseService.getExpenseById(expenseId);
        return ResponseEntity.ok(toResponse(expense));
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> updateExpense(@PathVariable Long expenseId, @RequestBody ExpenseRequest expenseRequest){
        Expense expense= expenseService.updateExpense(expenseId,expenseRequest);
        return ResponseEntity.ok(toResponse(expense));
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> archiveExpense(@PathVariable Long expenseId){
        Expense expense= expenseService.archiveExpense(expenseId);
        return ResponseEntity.ok(toResponse(expense));
    }

    private ExpenseResponse toResponse(Expense expense){
        return new ExpenseResponse(
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
    }
}

