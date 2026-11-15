package com.ga.finance_service.controller;

import com.ga.finance_service.client.CompanyClient;
import com.ga.finance_service.model.Expense;
import com.ga.finance_service.model.request.ExpenseRequest;
import com.ga.finance_service.model.response.ExpenseResponse;
import com.ga.finance_service.model.response.ProjectResponse;
import com.ga.finance_service.model.response.UserResponse;
import com.ga.finance_service.service.ExpenseService;
import jakarta.validation.Valid;
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
    private CompanyClient companyClient;

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody ExpenseRequest expenseRequest){
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
    public ResponseEntity<ExpenseResponse> updateExpense(@PathVariable Long expenseId, @Valid @RequestBody ExpenseRequest expenseRequest){
        Expense expense= expenseService.updateExpense(expenseId,expenseRequest);
        return ResponseEntity.ok(toResponse(expense));
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> archiveExpense(@PathVariable Long expenseId){
        Expense expense= expenseService.archiveExpense(expenseId);
        return ResponseEntity.ok(toResponse(expense));
    }

    private ExpenseResponse toResponse(Expense expense){
        UserResponse currentUser= ExpenseService.getCurrentLoggedInUser();
        String recordedByName = companyClient.getMemberName(expense.getProjectId(), expense.getMemberId(), currentUser.getId());
        ProjectResponse project= companyClient.getProjectById(expense.getProjectId(), currentUser.getId());

        return new ExpenseResponse(
                expense.getId(),
                expense.getProjectId(),
                project.getName(),
                expense.getMemberId(),
                recordedByName,
                expense.getTitle(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getDescription(),
                expense.getStatus(),
                expense.getExpenseDateTime()
        );
    }
}

