package com.ga.company_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    @Column(unique = true)
    private String callToken;

    @JsonIgnore
    @Column
    private String twilioCallSid;

    @JsonIgnore
    @Column
    private LocalDateTime callTokenExpiresAt;

    @JsonIgnore
    @Column(nullable = false)
    private boolean voiceCallRequested = false;

    @JsonIgnore
    @Column(nullable = false)
    private boolean voiceCallAccepted = false;

    @JsonIgnore
    @Column
    private LocalDateTime voiceCallRequestedAt;
}
