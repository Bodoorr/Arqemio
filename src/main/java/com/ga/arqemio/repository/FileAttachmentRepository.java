package com.ga.arqemio.repository;

import com.ga.arqemio.model.FileAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {
    List<FileAttachment> findByProjectId(Long projectId);
    List<FileAttachment> findByProjectUpdateId(Long projectUpdateId);
    List<FileAttachment> findByExpenseId(Long expenseId);
}
