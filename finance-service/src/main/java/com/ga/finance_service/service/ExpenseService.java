package com.ga.finance_service.service;

import com.ga.finance_service.client.AuditLogClient;
import com.ga.finance_service.client.CompanyClient;
import com.ga.finance_service.model.Expense;
import com.ga.finance_service.model.request.ExpenseRequest;
import com.ga.finance_service.model.response.ProjectResponse;
import com.ga.finance_service.model.response.UserResponse;
import com.ga.finance_service.repository.ExpenseRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@AllArgsConstructor
public class ExpenseService {
    private ExpenseRepository expenseRepository;
    private CompanyClient companyClient;
    private AuditLogClient auditLogClient;
    private static final Logger logger= Logger.getLogger(ExpenseService.class.getName());

    public static UserResponse getCurrentLoggedInUser(){
        return (UserResponse) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public Expense createExpense(ExpenseRequest expenseRequest){
        UserResponse currentUser= getCurrentLoggedInUser();
        Long membershipId= companyClient.validateExpenseAccess(expenseRequest.getProjectId(), currentUser.getId());

        validateExpenseAmount(expenseRequest);

        Expense expense=new Expense();
        expense.setProjectId(expenseRequest.getProjectId());
        expense.setMemberId(membershipId);
        expense.setTitle(expenseRequest.getTitle());
        expense.setAmount(expenseRequest.getAmount());
        expense.setCategory(expenseRequest.getCategory());
        expense.setDescription(expenseRequest.getDescription());
        expense.setExpenseDateTime(expenseRequest.getExpenseDateTime());
        expense.setStatus("ACTIVE");

        Expense savedExpense= expenseRepository.save(expense);
        ProjectResponse project= companyClient.getProjectById(savedExpense.getProjectId(), currentUser.getId());
        auditLogClient.createAuditLog(currentUser.getId(),project.getCompanyId(),"CREATE","EXPENSE",savedExpense.getId(),"Created expense: "+savedExpense.getTitle());
        logger.log(Level.INFO, "Expense {0} created for project {1} by user {2}", new Object[]{savedExpense.getId(), savedExpense.getProjectId(), currentUser.getId()});
        return savedExpense;
    }

    public List<Expense> getAllExpenses(){
        UserResponse currentUser= getCurrentLoggedInUser();
        List<Expense> expenses= new ArrayList<>();

        ProjectResponse[] projects= companyClient.getAllProjects(currentUser.getId());
        for (ProjectResponse project: projects){
            List<Expense> projectExpenses= expenseRepository.findByProjectId(project.getId());

            for (Expense expense: projectExpenses){
                expenses.add(expense);
            }
        }
        return expenses;
    }

    public Expense getExpenseById(Long expenseId){
        UserResponse currentUser= getCurrentLoggedInUser();
        Expense expense= expenseRepository.findById(expenseId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found."));

        companyClient.getProjectById(expense.getProjectId(), currentUser.getId());
        return expense;
    }

    public Expense updateExpense(Long expenseId, ExpenseRequest expenseRequest){
        UserResponse currentUser= getCurrentLoggedInUser();
        Expense expense= expenseRepository.findById(expenseId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Expense not found."));

        companyClient.validateExpenseAccess(expense.getProjectId(), currentUser.getId());
        companyClient.validateExpenseOwnership(expense.getProjectId(), expense.getMemberId(), currentUser.getId());

        validateExpenseAmount(expenseRequest);

        expense.setTitle(expenseRequest.getTitle());
        expense.setAmount(expenseRequest.getAmount());
        expense.setCategory(expenseRequest.getCategory());
        expense.setDescription(expenseRequest.getDescription());
        expense.setExpenseDateTime(expenseRequest.getExpenseDateTime());

        Expense updatedExpense= expenseRepository.save(expense);
        ProjectResponse project= companyClient.getProjectById(updatedExpense.getProjectId(), currentUser.getId());
        auditLogClient.createAuditLog(currentUser.getId(),project.getCompanyId(),"UPDATE", "EXPENSE",updatedExpense.getId(),"Updated expense: "+updatedExpense.getTitle());
        logger.log(Level.INFO,"Expense {0} updated by user {1}", new Object[]{expenseId, currentUser.getId()});
        return updatedExpense;
    }

    public Expense archiveExpense(Long expenseId){
        UserResponse currentUser= getCurrentLoggedInUser();
        Expense expense= expenseRepository.findById(expenseId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Expense not found."));

        companyClient.validateExpenseAccess(expense.getProjectId(), currentUser.getId());
        companyClient.validateExpenseOwnership(expense.getProjectId(), expense.getMemberId(), currentUser.getId());

        expense.setStatus("ARCHIVED");

        Expense archivedExpense= expenseRepository.save(expense);
        ProjectResponse project= companyClient.getProjectById(archivedExpense.getProjectId(), currentUser.getId());
        auditLogClient.createAuditLog(currentUser.getId(),project.getCompanyId(),"ARCHIVE","EXPENSE",archivedExpense.getId(),"Archived expense: "+archivedExpense.getTitle());
        logger.log(Level.INFO,"Expense {0} archived by user {1}", new Object[]{expenseId, currentUser.getId()});
        return archivedExpense;
    }

    public Long findExpenseProjectId(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found."));
        return expense.getProjectId();
    }

    public Long findExpenseMemberId(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found."));
        return expense.getMemberId();
    }

    private void validateExpenseAmount(ExpenseRequest expenseRequest){
        if (expenseRequest.getAmount()<=0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expense amount must be greater than 0.");
        }
    }
}
