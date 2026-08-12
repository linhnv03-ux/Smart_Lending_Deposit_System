package com.bank.slds.assessment.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_score_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditScoreRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String applicationNo;

    @Column(nullable = false)
    private String applicantId;

    @Column(nullable = false)
    private String applicantCip; // Căn cước công dân / CIP

    private Integer creditScore; // 300 - 850 score

    @Enumerated(EnumType.STRING)
    private DebtGroup cicDebtGroup; // Phân loại nợ CIC

    private boolean badDebtFound;

    private BigDecimal maxRecommendLoanLimit;

    private String decisionStatus; // APPROVED, REJECTED, MANUAL_REVIEW

    private String decisionReason;

    private LocalDateTime evaluatedAt;
}
