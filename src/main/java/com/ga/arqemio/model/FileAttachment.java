package com.ga.arqemio.model;

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
@ToString(exclude = {"project","expense","projectUpdate","uploadedBy"})
public class FileAttachment {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "expense_id")
    private Expense expense;

    @Column
    private String fileUrl;

    @Column
    private String fileName;

    @Column
    private String fileType;

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
