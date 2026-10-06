package com.ga.arqemio.controller;

import com.ga.arqemio.model.FileAttachment;
import com.ga.arqemio.model.response.FileAttachmentResponse;
import com.ga.arqemio.service.FileAttachmentService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/company/projects/attachments")
@AllArgsConstructor
public class FileAttachmentController {
    private FileAttachmentService fileAttachmentService;

    @PostMapping
    public ResponseEntity<FileAttachmentResponse> createFileAttachment(@RequestParam("file") MultipartFile file, @RequestParam Long projectId, @RequestParam(required = false) Long projectUpdateId, @RequestParam(required = false) Long expenseId){
        FileAttachment fileAttachment= fileAttachmentService.createFileAttachment(file,projectId,projectUpdateId,expenseId);
        FileAttachmentResponse fileAttachmentResponse=new FileAttachmentResponse(
                fileAttachment.getId(),
                fileAttachment.getProject().getId(),
                fileAttachment.getExpense() != null ? fileAttachment.getExpense().getId() : null,
                fileAttachment.getProjectUpdate() != null ? fileAttachment.getProjectUpdate().getId() : null,
                fileAttachment.getFileUrl(),
                fileAttachment.getFileName(),
                fileAttachment.getFileType(),
                fileAttachment.getUploadedBy().getId(),
                fileAttachment.getUploadedBy().getUser().getName(),
                fileAttachment.getStatus()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(fileAttachmentResponse);
    }

    @GetMapping("/{attachmentId}")
    public ResponseEntity<FileAttachmentResponse> getFileAttachment(@PathVariable Long attachmentId){
        FileAttachment fileAttachment= fileAttachmentService.getFileAttachmentById(attachmentId);
        FileAttachmentResponse fileAttachmentResponse=new FileAttachmentResponse(
                fileAttachment.getId(),
                fileAttachment.getProject().getId(),
                fileAttachment.getExpense() != null ? fileAttachment.getExpense().getId() : null,
                fileAttachment.getProjectUpdate() != null ? fileAttachment.getProjectUpdate().getId() : null,
                fileAttachment.getFileUrl(),
                fileAttachment.getFileName(),
                fileAttachment.getFileType(),
                fileAttachment.getUploadedBy().getId(),
                fileAttachment.getUploadedBy().getUser().getName(),
                fileAttachment.getStatus()
        );
        return ResponseEntity.ok(fileAttachmentResponse);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<FileAttachmentResponse>> getAllProjectAttachments(@PathVariable Long projectId){
        List<FileAttachment> fileAttachments= fileAttachmentService.getAllProjectAttachments(projectId);
        List<FileAttachmentResponse> fileAttachmentResponses= new ArrayList<>();

        for (FileAttachment fileAttachment : fileAttachments){
            FileAttachmentResponse fileAttachmentResponse=new FileAttachmentResponse(
                    fileAttachment.getId(),
                    fileAttachment.getProject().getId(),
                    fileAttachment.getExpense() != null ? fileAttachment.getExpense().getId() : null,
                    fileAttachment.getProjectUpdate() != null ? fileAttachment.getProjectUpdate().getId() : null,
                    fileAttachment.getFileUrl(),
                    fileAttachment.getFileName(),
                    fileAttachment.getFileType(),
                    fileAttachment.getUploadedBy().getId(),
                    fileAttachment.getUploadedBy().getUser().getName(),
                    fileAttachment.getStatus()
            );
            fileAttachmentResponses.add(fileAttachmentResponse);
        }
        return ResponseEntity.ok(fileAttachmentResponses);
    }

    @GetMapping("/update/{projectUpdateId}")
    public ResponseEntity<List<FileAttachmentResponse>> getAllProjectUpdateAttachments(@PathVariable Long projectUpdateId){
        List<FileAttachment> fileAttachments= fileAttachmentService.getAllProjectUpdateAttachments(projectUpdateId);
        List<FileAttachmentResponse> fileAttachmentResponses= new ArrayList<>();

        for (FileAttachment fileAttachment : fileAttachments){
            FileAttachmentResponse fileAttachmentResponse=new FileAttachmentResponse(
                    fileAttachment.getId(),
                    fileAttachment.getProject().getId(),
                    fileAttachment.getExpense() != null ? fileAttachment.getExpense().getId() : null,
                    fileAttachment.getProjectUpdate() != null ? fileAttachment.getProjectUpdate().getId() : null,
                    fileAttachment.getFileUrl(),
                    fileAttachment.getFileName(),
                    fileAttachment.getFileType(),
                    fileAttachment.getUploadedBy().getId(),
                    fileAttachment.getUploadedBy().getUser().getName(),
                    fileAttachment.getStatus()
            );
            fileAttachmentResponses.add(fileAttachmentResponse);
        }
        return ResponseEntity.ok(fileAttachmentResponses);
    }

    @GetMapping("/expense/{expenseId}")
    public ResponseEntity<List<FileAttachmentResponse>> getAllProjectExpenseAttachments(@PathVariable Long expenseId){
        List<FileAttachment> fileAttachments= fileAttachmentService.getAllProjectExpenseAttachments(expenseId);
        List<FileAttachmentResponse> fileAttachmentResponses= new ArrayList<>();

        for (FileAttachment fileAttachment : fileAttachments){
            FileAttachmentResponse fileAttachmentResponse=new FileAttachmentResponse(
                    fileAttachment.getId(),
                    fileAttachment.getProject().getId(),
                    fileAttachment.getExpense() != null ? fileAttachment.getExpense().getId() : null,
                    fileAttachment.getProjectUpdate() != null ? fileAttachment.getProjectUpdate().getId() : null,
                    fileAttachment.getFileUrl(),
                    fileAttachment.getFileName(),
                    fileAttachment.getFileType(),
                    fileAttachment.getUploadedBy().getId(),
                    fileAttachment.getUploadedBy().getUser().getName(),
                    fileAttachment.getStatus()
            );
            fileAttachmentResponses.add(fileAttachmentResponse);
        }
        return ResponseEntity.ok(fileAttachmentResponses);
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<FileAttachmentResponse> archiveFileAttachment(@PathVariable Long attachmentId){
        FileAttachment fileAttachment= fileAttachmentService.archiveFileAttachment(attachmentId);
        FileAttachmentResponse fileAttachmentResponse=new FileAttachmentResponse(
                fileAttachment.getId(),
                fileAttachment.getProject().getId(),
                fileAttachment.getExpense() != null ? fileAttachment.getExpense().getId() : null,
                fileAttachment.getProjectUpdate() != null ? fileAttachment.getProjectUpdate().getId() : null,
                fileAttachment.getFileUrl(),
                fileAttachment.getFileName(),
                fileAttachment.getFileType(),
                fileAttachment.getUploadedBy().getId(),
                fileAttachment.getUploadedBy().getUser().getName(),
                fileAttachment.getStatus()
        );
        return ResponseEntity.ok(fileAttachmentResponse);
    }
}
