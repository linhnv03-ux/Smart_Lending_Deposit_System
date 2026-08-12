package com.bank.slds.loan.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String applicationNo;

    @Column(nullable = false)
    private String applicantId;

    @Column(nullable = false)
    private String applicantName;

    @Column(nullable = false)
    private String applicantCip; // Căn cước công dân

    @Column(nullable = false)
    private BigDecimal requestedAmount;

    @Column(nullable = false)
    private Integer termMonths;

    @Column(nullable = false)
    private BigDecimal interestRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterestType interestType;

    @Column(length = 500)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    private Integer creditScore;

    private String officerId;

    private String branchCode;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = LoanStatus.SUBMITTED;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
