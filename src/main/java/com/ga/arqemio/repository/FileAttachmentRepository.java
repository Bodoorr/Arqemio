package com.ga.arqemio.repository;

import com.ga.arqemio.model.FileAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {
    List<FileAttachment> findByProjectIdAndStatus(Long projectId, String status);
    List<FileAttachment> findByProjectUpdateIdAndStatus(Long projectUpdateId, String status);
    List<FileAttachment> findByExpenseIdAndStatus(Long expenseId, String status);
}
