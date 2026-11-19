package com.ga.company_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "file_attachments")
@ToString(exclude = {"project","projectUpdate","uploadedBy"})
public class FileAttachment {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "expense_id")
    private Long expenseId;

    @Column
    private String fileUrl;

    @Column
    private String fileName;

    @Column
    private String fileType;

    @Column
    private String status = "ACTIVE";

    @ManyToOne
    @JoinColumn(name = "uploaded_by", nullable = false)
    private CompanyMembership uploadedBy;

    @ManyToOne
    @JoinColumn(name = "project_update_id")
    private ProjectUpdate projectUpdate;

    @Column
    @CreationTimestamp
    private LocalDateTime createdAt;


}
