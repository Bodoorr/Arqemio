package com.ga.arqemio.service;

import com.ga.arqemio.model.CompanyMembership;
import com.ga.arqemio.model.Expense;
import com.ga.arqemio.model.Project;
import com.ga.arqemio.model.User;
import com.ga.arqemio.model.request.ExpenseRequest;
import com.ga.arqemio.repository.CompanyMembershipRepository;
import com.ga.arqemio.repository.ExpenseRepository;
import com.ga.arqemio.repository.ProjectRepository;
import com.ga.arqemio.security.MyUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class ExpenseService {
    private ExpenseRepository expenseRepository;
    private ProjectRepository projectRepository;
    private CompanyMembershipRepository companyMembershipRepository;

    public static User getCurrentLoggedInUser(){
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    public Expense createExpense(ExpenseRequest expenseRequest){
        User currentUser= getCurrentLoggedInUser();
        Project project= projectRepository.findById(expenseRequest.getProjectId()).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
        boolean isCompanyOwner= companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(currentUser.getId(), project.getCompany().getId(), "OWNER", "ACTIVE");
        boolean isAssignedManager= projectRepository.existsByIdAndManagersUserIdAndManagersStatus(project.getId(), currentUser.getId(), "ACTIVE");

        if (!isCompanyOwner && !isAssignedManager){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to create expenses for this project.");
        }

        CompanyMembership membership= companyMembershipRepository.findByUserIdAndCompanyIdAndStatus(currentUser.getId(),project.getCompany().getId(), "ACTIVE")
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.FORBIDDEN, "Active company membership is not found."));

        if (expenseRequest.getAmount()<=0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expense amount must be greater than 0.");
        }

        Expense expense=new Expense();
        expense.setProject(project);
        expense.setMember(membership);
        expense.setTitle(expenseRequest.getTitle());
        expense.setAmount(expenseRequest.getAmount());
        expense.setCategory(expenseRequest.getCategory());
        expense.setDescription(expenseRequest.getDescription());
        expense.setExpenseDateTime(expenseRequest.getExpenseDateTime());
        expense.setStatus("ACTIVE");

        return expenseRepository.save(expense);
    }

    public List<Expense> getAllExpenses(){
        User currentUser= getCurrentLoggedInUser();
        boolean isPlatformAdmin= currentUser.getIsPlatformAdmin().equals(true);
        if (isPlatformAdmin){
            return expenseRepository.findAll();
        }

        return expenseRepository.findByProjectCompanyMembershipsUserIdAndProjectCompanyMembershipsStatus(currentUser.getId(), "ACTIVE");
    }

    public Expense getExpenseById(Long expenseId){
        User currentUser= getCurrentLoggedInUser();
        Expense expense= expenseRepository.findById(expenseId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found."));
        boolean isPlatformAdmin= currentUser.getIsPlatformAdmin().equals(true);
        boolean isActiveMember= companyMembershipRepository.existsByUserIdAndCompanyIdAndStatus(currentUser.getId(), expense.getProject().getCompany().getId(), "ACTIVE");

        if (!isPlatformAdmin && !isActiveMember){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to view this expense.");
        }
        return expense;
    }
}
