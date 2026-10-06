package com.ga.arqemio.service;

import com.ga.arqemio.model.*;
import com.ga.arqemio.repository.*;
import com.ga.arqemio.security.MyUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.cloudinary.*;
import com.cloudinary.utils.ObjectUtils;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
@Service
@AllArgsConstructor
public class FileAttachmentService {
    private final Dotenv dotenv = Dotenv.load();
    private final Cloudinary cloudinary = new Cloudinary(dotenv.get("CLOUDINARY_URL"));
    private FileAttachmentRepository fileAttachmentRepository;
    private ProjectRepository projectRepository;
    private ProjectUpdateRepository projectUpdateRepository;
    private ExpenseRepository expenseRepository;
    private  CompanyMembershipRepository companyMembershipRepository;

    public static User getCurrentLoggedInUser(){
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    public Map uploadImage(MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Please select an image to upload");
        }

        if (file.getContentType() == null ||
                !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "arqemio",
                        "resource_type", "image",
                        "use_filename", true,
                        "unique_filename", true,
                        "overwrite", false
                )
        );

        return uploadResult;
    }

    public FileAttachment createFileAttachment(MultipartFile file, Long projectId, Long projectUpdateId, Long expenseId){
        User currentUser= getCurrentLoggedInUser();
        Project project= projectRepository.findById(projectId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Project not found."));
        CompanyMembership membership= companyMembershipRepository.findByUserIdAndCompanyIdAndStatus(currentUser.getId(),project.getCompany().getId(), "ACTIVE")
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.FORBIDDEN,"You're not an active member of this company."));

        ProjectUpdate projectUpdate= null;
        if (projectUpdateId != null){
            projectUpdate= projectUpdateRepository.findById(projectUpdateId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Project update not found."));

            if (!projectUpdate.getProject().getId().equals(projectId)){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Project update does not belong to this project.");
            }
        }

        Expense expense= null;
        if (expenseId != null){
            expense= expenseRepository.findById(expenseId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Expense not found."));

            if (!expense.getProject().getId().equals(projectId)){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Expense does not belong to this project.");
            }
        }

        try {
            Map uploadResult= uploadImage(file);
            FileAttachment fileAttachment= new FileAttachment();
            fileAttachment.setProject(project);
            fileAttachment.setProjectUpdate(projectUpdate);
            fileAttachment.setExpense(expense);
            fileAttachment.setFileUrl(uploadResult.get("secure_url").toString());
            fileAttachment.setFileName(file.getOriginalFilename());
            fileAttachment.setFileType(file.getContentType());
            fileAttachment.setUploadedBy(membership);

            return fileAttachmentRepository.save(fileAttachment);
        } catch (IOException e){
            throw new RuntimeException("Failed to upload image.");
        }
    }

    public List<FileAttachment> getAllProjectAttachments(Long projectId){
        User currentUser= getCurrentLoggedInUser();
        Project project= projectRepository.findById(projectId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Project not found."));
        boolean isActiveMember= companyMembershipRepository.existsByUserIdAndCompanyIdAndStatus(currentUser.getId(), project.getCompany().getId(), "ACTIVE");
        if (!currentUser.getIsPlatformAdmin() && !isActiveMember){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this project.");
        }

        return fileAttachmentRepository.findByProjectIdAndStatus(projectId, "ACTIVE");
    }

    public List<FileAttachment> getAllProjectUpdateAttachments(Long projectUpdateId){
        User currentUser= getCurrentLoggedInUser();
        ProjectUpdate projectUpdate= projectUpdateRepository.findById(projectUpdateId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Project update not found."));
        boolean isActiveMember= companyMembershipRepository.existsByUserIdAndCompanyIdAndStatus(currentUser.getId(), projectUpdate.getProject().getCompany().getId(), "ACTIVE");
        if (!currentUser.getIsPlatformAdmin() && !isActiveMember){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this project.");
        }

        return fileAttachmentRepository.findByProjectUpdateIdAndStatus(projectUpdateId, "ACTIVE");
    }

    public List<FileAttachment> getAllProjectExpenseAttachments(Long expenseId){
        User currentUser= getCurrentLoggedInUser();
        Expense expense= expenseRepository.findById(expenseId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Expense not found."));
        boolean isActiveMember= companyMembershipRepository.existsByUserIdAndCompanyIdAndStatus(currentUser.getId(), expense.getProject().getCompany().getId(), "ACTIVE");
        if (!currentUser.getIsPlatformAdmin() && !isActiveMember){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this project.");
        }

        return fileAttachmentRepository.findByExpenseIdAndStatus(expenseId, "ACTIVE");
    }

    public FileAttachment getFileAttachmentById(Long attachmentId){
        User currentUser= getCurrentLoggedInUser();
        FileAttachment fileAttachment= fileAttachmentRepository.findById(attachmentId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "File attachment not found."));
        boolean isActiveMember= companyMembershipRepository.existsByUserIdAndCompanyIdAndStatus(currentUser.getId(), fileAttachment.getProject().getCompany().getId(), "ACTIVE");
        if (!currentUser.getIsPlatformAdmin() && !isActiveMember){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this project.");
        }
        if (!fileAttachment.getStatus().equals("ACTIVE")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File attachment not found.");
        }
        return fileAttachment;
    }

    public FileAttachment archiveFileAttachment(Long attachmentId) {
        User currentUser = getCurrentLoggedInUser();
        FileAttachment fileAttachment = fileAttachmentRepository.findById(attachmentId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File attachment not found."));

        if (!fileAttachment.getStatus().equals("ACTIVE")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File attachment is already archived.");
        }

        Project project = fileAttachment.getProject();

        boolean isUploader = fileAttachment.getUploadedBy().getUser().getId().equals(currentUser.getId());
        boolean isOwner = companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(currentUser.getId(), project.getCompany().getId(), "OWNER", "ACTIVE");
        boolean isManager = projectRepository.existsByIdAndManagersUserIdAndManagersStatus(project.getId(), currentUser.getId(), "ACTIVE");

        if (!currentUser.getIsPlatformAdmin() && !isUploader && !isOwner && !isManager) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to archive this attachment.");
        }

        fileAttachment.setStatus("ARCHIVED");
        return fileAttachmentRepository.save(fileAttachment);
    }




}
