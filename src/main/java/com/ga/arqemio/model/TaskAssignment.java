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
@Table(name = "task_assignments")
@ToString(exclude = {"task","assignedBy", "assignedTo"})
public class TaskAssignment {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne
    @JoinColumn(name = "assigned_by", nullable = false)
    private CompanyMembership assignedBy;

    @ManyToOne
    @JoinColumn(name = "assigned_to", nullable = false)
    private CompanyMembership assignedTo;

    @Column
    private LocalDateTime assignedAt;

    @Column
    private LocalDateTime dueAt;

    @Column
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column
    @UpdateTimestamp
    private LocalDateTime updatedAt;




}
