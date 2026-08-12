package com.bank.slds.assessment.dto;

import com.bank.slds.assessment.model.DebtGroup;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreditEvaluationResult(
    String evaluationId,
    String applicationNo,
    String applicantCip,
    String applicantName,
    int creditScore,
    DebtGroup cicDebtGroup,
    boolean badDebtFound,
    BigDecimal approvedLoanLimit,
    String decisionStatus,
    String decisionReason,
    LocalDateTime evaluatedAt,
    String elasticsearchAuditId
) {}
