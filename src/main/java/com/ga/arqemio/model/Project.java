package com.ga.arqemio.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "projects")
@ToString(exclude = {"company","managers", "tasks"})
public class Project {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToMany
    @JoinTable(name = "project_managers", joinColumns = @JoinColumn(name = "project_id"), inverseJoinColumns = @JoinColumn(name = "membership_id"))
    private List<CompanyMembership> managers= new ArrayList<>();

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private String location;

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate expectedEndDate;

    @Column
    private double budget;

    @Column
    private String status;

    @Column
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "project")
    private List<Task> tasks = new ArrayList<>();
}
