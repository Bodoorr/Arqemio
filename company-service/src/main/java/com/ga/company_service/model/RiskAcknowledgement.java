package com.ga.company_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "risk_acknowledgements", uniqueConstraints = @UniqueConstraint(columnNames = {"risk_id", "worker_id"}))
@ToString(exclude = {"risk", "worker"})
public class RiskAcknowledgement {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "risk_id", nullable = false)
    private Risk risk;

    @ManyToOne
    @JoinColumn(name = "worker_id", nullable = false)
    private CompanyMembership worker;


    @Column(nullable = false)
    private String status = "PENDING";

    @Column
    private LocalDateTime respondedAt;

    @Column(unique = true)
    private String callToken;

    @Column
    private String twilioCallSid;

    @Column
    private LocalDateTime callTokenExpiresAt;
}
