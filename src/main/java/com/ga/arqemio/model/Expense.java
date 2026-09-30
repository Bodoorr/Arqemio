package com.ga.arqemio.model;

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
@Table(name = "expenses")
@ToString(exclude = {"project", "member"})
public class Expense {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project ;

    @ManyToOne
    @JoinColumn(name = "recorded_by", nullable = false)
    private CompanyMembership member;

    @Column
    private String title;

    @Column
    private double amount;

    @Column
    private String category;

    @Column
    private String description;

    @Column
    private String status;

    @Column
    private LocalDateTime expenseDateTime;

    @Column
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column
    @UpdateTimestamp
    private LocalDateTime updatedAt;



}
