package com.bank.slds.loan.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String contractNo;

    @Column(nullable = false)
    private String applicationNo;

    @Column(nullable = false)
    private String applicantId;

    @Column(nullable = false)
    private String applicantName;

    @Column(nullable = false)
    private BigDecimal principalAmount;

    @Column(nullable = false)
    private BigDecimal currentBalance;

    @Column(nullable = false)
    private BigDecimal interestRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterestType interestType;

    @Column(nullable = false)
    private Integer termMonths;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    private String disbursementAccount;

    private LocalDateTime disbursedAt;

    private LocalDateTime createdAt;
}
