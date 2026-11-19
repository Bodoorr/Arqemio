package com.ga.company_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ga.company_service.client.FinanceClient;
import com.ga.company_service.model.*;
import com.ga.company_service.model.response.UserResponse;
import com.ga.company_service.repository.*;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class FileAttachmentService {
    private final Dotenv dotenv = Dotenv.load();
    private final Cloudinary cloudinary = new Cloudinary(dotenv.get("CLOUDINARY_URL"));
    private final FileAttachmentRepository fileAttachmentRepository;
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;
    private final ProjectUpdateRepository projectUpdateRepository;
    private final CompanyMembershipRepository companyMembershipRepository;
    private final RiskRepository riskRepository;
    private final FinanceClient financeClient;

    private static final Logger logger= Logger.getLogger(FileAttachmentService.class.getName());

    public static UserResponse getCurrentLoggedInUser(){
        return (UserResponse) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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

    public Map uploadConstructionFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select a file.");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name.");
        }

        String name = fileName.toLowerCase();
        boolean isImage = name.endsWith(".png")
                || name.endsWith(".jpg")
                || name.endsWith(".jpeg");

        boolean isPdf = name.endsWith(".pdf");
        boolean isModel = name.endsWith(".glb")
                || name.endsWith(".gltf");

        if (!isImage && !isPdf && !isModel) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only PNG, JPG, PDF, GLB and GLTF files are allowed.");
        }

        String resourceType = isImage ? "image" : "raw";
        return cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "arqemio/construction",
                        "resource_type", resourceType,
                        "use_filename", true,
                        "unique_filename", true,
                        "overwrite", false
                )
        );
    }

    public FileAttachment createFileAttachment(MultipartFile file, Long projectId, Long projectUpdateId, Long expenseId, Long riskId){
        UserResponse currentUser= getCurrentLoggedInUser();
        Project project= projectRepository.findById(projectId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Project not found."));
        CompanyMembership membership= companyMembershipRepository.findByUserIdAndCompanyIdAndStatus(currentUser.getId(),project.getCompany().getId(), "ACTIVE")
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.FORBIDDEN,"You're not an active member of this company."));

        boolean isOwner = membership.getRole().equals("OWNER");
        boolean isManager = projectRepository.existsByIdAndManagersUserIdAndManagersStatus(project.getId(), currentUser.getId(), "ACTIVE");
        boolean isWorker = projectRepository.existsByIdAndWorkersUserIdAndWorkersStatus(project.getId(), currentUser.getId(), "ACTIVE");

        if (!isOwner && !isManager && !isWorker) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this project.");
        }

        ProjectUpdate projectUpdate= null;
        if (projectUpdateId != null){
            projectUpdate= projectUpdateRepository.findById(projectUpdateId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Project update not found."));

            if (!projectUpdate.getProject().getId().equals(projectId)){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Project update does not belong to this project.");
            }

            if (!projectUpdate.getUpdatedBy().getUserId().equals(currentUser.getId())){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only attach files to your own project updates.");
            }
        }

        if (expenseId != null){
            Long expenseProjectId= financeClient.getExpenseProjectId(expenseId);
            Long expenseMemberId= financeClient.getExpenseMemberId(expenseId);

            if (!expenseProjectId.equals(projectId)){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Expense does not belong to this project.");
            }

            if (!expenseMemberId.equals(membership.getId())){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only attach files to your own expenses.");
            }
        }

        Risk risk = null;

        if (riskId != null) {
            risk = riskRepository.findById(riskId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Risk not found."));

            if (!risk.getProject().getId().equals(projectId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Risk does not belong to this project.");
            }

            if (!risk.getReportedBy().getUserId().equals(currentUser.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only attach files to risks you reported.");
            }

            if (!"PENDING_REVIEW".equals(risk.getStatus())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can only add evidence before the risk is reviewed.");
            }
        }

        try {
            Map uploadResult= uploadImage(file);
            FileAttachment fileAttachment= new FileAttachment();
            fileAttachment.setProject(project);
            fileAttachment.setProjectUpdate(projectUpdate);
            fileAttachment.setExpenseId(expenseId);
            fileAttachment.setRisk(risk);
            fileAttachment.setFileUrl(uploadResult.get("secure_url").toString());
            fileAttachment.setFileName(file.getOriginalFilename());
            fileAttachment.setFileType(file.getContentType());
            fileAttachment.setUploadedBy(membership);

            FileAttachment savedAttachment= fileAttachmentRepository.save(fileAttachment);
            logger.log(Level.INFO,"File attachment {0} uploaded to project {1} by user {2}", new Object[]{savedAttachment.getId(), projectId, currentUser.getId()});
            return savedAttachment;
        } catch (IOException e){
            throw new RuntimeException("Failed to upload image.");
        }
    }

    public FileAttachment createConstructionDrawing(MultipartFile file, Long projectId) {
        UserResponse currentUser = getCurrentLoggedInUser();

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));

        CompanyMembership membership = companyMembershipRepository.findByUserIdAndCompanyIdAndStatus(currentUser.getId(), project.getCompany().getId(), "ACTIVE")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not an active member of this company."));

        boolean isOwner = membership.getRole().equals("OWNER");
        boolean isManager = "MANAGER".equals(membership.getRole()) && projectRepository.existsByIdAndManagersUserIdAndManagersStatus(projectId, currentUser.getId(), "ACTIVE");
        if (!isOwner && !isManager) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only project owners and managers can upload construction drawings.");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name.");
        }

        String name = fileName.toLowerCase();
        if (!name.endsWith(".png") && !name.endsWith(".jpg") && !name.endsWith(".jpeg") && !name.endsWith(".pdf")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Construction drawings must be PNG, JPG or PDF.");
        }

        try {
            Map uploadResult = uploadConstructionFile(file);

            FileAttachment attachment = new FileAttachment();
            attachment.setProject(project);
            attachment.setUploadedBy(membership);
            attachment.setFileUrl(uploadResult.get("secure_url").toString());
            attachment.setFileName(fileName);
            attachment.setFileType(file.getContentType());
            attachment.setAttachmentType("CONSTRUCTION_DRAWING");
            attachment.setConversionStatus("PENDING");

            return fileAttachmentRepository.save(attachment);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to upload construction drawing.");
        }
    }

    public List<FileAttachment> getAllProjectAttachments(Long projectId){
        projectService.getProjectById(projectId);
        return fileAttachmentRepository.findByProjectIdAndStatus(projectId, "ACTIVE");
    }

    public List<FileAttachment> getAllProjectUpdateAttachments(Long projectUpdateId){
        ProjectUpdate projectUpdate= projectUpdateRepository.findById(projectUpdateId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Project update not found."));
        projectService.getProjectById(projectUpdate.getProject().getId());
        return fileAttachmentRepository.findByProjectUpdateIdAndStatus(projectUpdateId, "ACTIVE");
    }

    public List<FileAttachment> getAllProjectExpenseAttachments(Long expenseId){
        Long projectId= financeClient.getExpenseProjectId(expenseId);
        projectService.getProjectById(projectId);
        return fileAttachmentRepository.findByExpenseIdAndStatus(expenseId, "ACTIVE");
    }

    public List<FileAttachment> getAllProjectRiskAttachments(Long riskId) {
        Risk risk = riskRepository.findById(riskId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Risk not found."));
        projectService.getProjectById(risk.getProject().getId());
        return fileAttachmentRepository.findByRiskIdAndStatus(riskId, "ACTIVE");
    }

    public FileAttachment getFileAttachmentById(Long attachmentId){
        FileAttachment fileAttachment= fileAttachmentRepository.findById(attachmentId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "File attachment not found."));
        projectService.getProjectById(fileAttachment.getProject().getId());
        if (!fileAttachment.getStatus().equals("ACTIVE")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File attachment not found.");
        }
        return fileAttachment;
    }

    public FileAttachment archiveFileAttachment(Long attachmentId) {
        UserResponse currentUser = getCurrentLoggedInUser();
        FileAttachment fileAttachment = fileAttachmentRepository.findById(attachmentId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File attachment not found."));

        if (!fileAttachment.getStatus().equals("ACTIVE")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File attachment is already archived.");
        }

        Project project = fileAttachment.getProject();

        projectService.getProjectById(project.getId());
        boolean isUploader = fileAttachment.getUploadedBy().getUserId().equals(currentUser.getId());

        if (!isUploader){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only archive your own attachments.");
        }

        fileAttachment.setStatus("ARCHIVED");
        FileAttachment archivedAttachment= fileAttachmentRepository.save(fileAttachment);
        logger.log(Level.INFO,"File attachment {0} archived by user {1}", new Object[]{attachmentId, currentUser.getId()});
        return archivedAttachment;
    }


}
