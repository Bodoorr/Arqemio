package com.ga.company_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "risks")
@ToString(exclude = {"project", "reportedBy", "approvedBy", "assignedTo"})
public class Risk {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "projectId", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String status;

    @Column
    private Integer likelihood;

    @Column
    private Integer impact;

    @Column
    private Integer riskScore;

    @Column
    private String level;

    @Column(nullable = false)
    private Boolean evacuationRequired= false;

    @ManyToOne
    @JoinColumn(name = "reportedBy", nullable = false)
    private CompanyMembership reportedBy;

    @ManyToOne
    @JoinColumn(name = "approvedBy")
    private CompanyMembership approvedBy;

    @ManyToOne
    @JoinColumn(name = "assignedTo")
    private CompanyMembership assignedTo;

    @Column
    private String action;

    @Column
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime resolvedAt;

}
