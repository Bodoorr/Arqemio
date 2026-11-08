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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@AllArgsConstructor
public class ExpenseService {
    private ExpenseRepository expenseRepository;
    private ProjectRepository projectRepository;
    private ProjectService projectService;
    private CompanyMembershipRepository companyMembershipRepository;
    private AuditLogService auditLogService;
    private static final Logger logger= Logger.getLogger(ExpenseService.class.getName());

    public static User getCurrentLoggedInUser(){
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    public Expense createExpense(ExpenseRequest expenseRequest){
        User currentUser= getCurrentLoggedInUser();
        Project project= projectRepository.findById(expenseRequest.getProjectId()).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
        validateExpenseManagementAccess(currentUser, project);

        CompanyMembership membership= companyMembershipRepository.findByUserIdAndCompanyIdAndStatus(currentUser.getId(),project.getCompany().getId(), "ACTIVE")
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.FORBIDDEN, "Active company membership is not found."));

        validateExpenseAmount(expenseRequest);

        Expense expense=new Expense();
        expense.setProject(project);
        expense.setMember(membership);
        expense.setTitle(expenseRequest.getTitle());
        expense.setAmount(expenseRequest.getAmount());
        expense.setCategory(expenseRequest.getCategory());
        expense.setDescription(expenseRequest.getDescription());
        expense.setExpenseDateTime(expenseRequest.getExpenseDateTime());
        expense.setStatus("ACTIVE");

        Expense savedExpense= expenseRepository.save(expense);
        auditLogService.createAuditLog(currentUser,project.getCompany(),"CREATE","EXPENSE",savedExpense.getId(),"Created expense: "+savedExpense.getTitle());
        logger.log(Level.INFO, "Expense {0} created for project {1} by user {2}", new Object[]{savedExpense.getId(), project.getId(), currentUser.getId()});
        return savedExpense;
    }

    public List<Expense> getAllExpenses(){
        List<Expense> expenses= new ArrayList<>();
        List<Project> projects= projectService.getAllProjects();

        for (Project project: projects){
            List<Expense> projectExpenses= expenseRepository.findByProjectId(project.getId());

            for (Expense expense: projectExpenses){
                expenses.add(expense);
            }
        }
        return expenses;
    }

    public Expense getExpenseById(Long expenseId){
        Expense expense= expenseRepository.findById(expenseId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found."));
        projectService.getProjectById(expense.getProject().getId());
        return expense;
    }

    public Expense updateExpense(Long expenseId, ExpenseRequest expenseRequest){
        User currentUser= getCurrentLoggedInUser();
        Expense expense= expenseRepository.findById(expenseId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Expense not found."));
        validateExpenseManagementAccess(currentUser, expense.getProject());

        if (!expense.getMember().getUser().getId().equals(currentUser.getId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own expenses.");
        }

        validateExpenseAmount(expenseRequest);

        expense.setTitle(expenseRequest.getTitle());
        expense.setAmount(expenseRequest.getAmount());
        expense.setCategory(expenseRequest.getCategory());
        expense.setDescription(expenseRequest.getDescription());
        expense.setExpenseDateTime(expenseRequest.getExpenseDateTime());

        Expense updatedExpense= expenseRepository.save(expense);
        auditLogService.createAuditLog(currentUser, updatedExpense.getProject().getCompany(),"UPDATE", "EXPENSE",updatedExpense.getId(),"Updated expense: "+updatedExpense.getTitle());
        logger.log(Level.INFO,"Expense {0} updated by user {1}", new Object[]{expenseId, currentUser.getId()});
        return updatedExpense;
    }

    public Expense archiveExpense(Long expenseId){
        User currentUser= getCurrentLoggedInUser();
        Expense expense= expenseRepository.findById(expenseId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Expense not found."));
        validateExpenseManagementAccess(currentUser, expense.getProject());
        if (!expense.getMember().getUser().getId().equals(currentUser.getId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only archive your own expenses.");
        }
        expense.setStatus("ARCHIVED");

        Expense archivedExpense= expenseRepository.save(expense);
        auditLogService.createAuditLog(currentUser,archivedExpense.getProject().getCompany(),"ARCHIVE","EXPENSE",archivedExpense.getId(),"Archived expense: "+archivedExpense.getTitle());
        logger.log(Level.INFO,"Expense {0} archived by user {1}", new Object[]{expenseId, currentUser.getId()});
        return archivedExpense;
    }

    private void validateExpenseManagementAccess(User user, Project project){
        boolean isCompanyOwner= companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(user.getId(), project.getCompany().getId(), "OWNER", "ACTIVE");
        boolean isActiveMember= companyMembershipRepository.existsByUserIdAndCompanyIdAndStatus(user.getId(),project.getCompany().getId(), "ACTIVE");
        boolean isAssignedManager= projectRepository.existsByIdAndManagersUserIdAndManagersStatus(project.getId(), user.getId(), "ACTIVE");

        if (!isCompanyOwner && !(isAssignedManager && isActiveMember)){
            logger.log(Level.WARNING,"User {0} attempted to manage expenses for project {1} without permission",new Object[]{user.getId(), project.getId()});
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to manage expenses for this project.");
        }
    }

    private void validateExpenseAmount(ExpenseRequest expenseRequest){
        if (expenseRequest.getAmount()<=0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expense amount must be greater than 0.");
        }
    }
}
