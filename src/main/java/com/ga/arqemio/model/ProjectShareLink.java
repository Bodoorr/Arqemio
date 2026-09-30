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
@Table(name="project_share_links")
@ToString(exclude = {"projectUpdate","createdBy","token"})
public class ProjectShareLink {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_update_id", nullable = false)
    private ProjectUpdate projectUpdate;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private CompanyMembership createdBy;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime revokedAt;



}
