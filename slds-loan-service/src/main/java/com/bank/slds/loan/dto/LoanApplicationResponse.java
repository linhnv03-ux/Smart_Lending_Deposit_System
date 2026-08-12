package com.bank.slds.loan.dto;

import com.bank.slds.loan.model.InterestType;
import com.bank.slds.loan.model.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoanApplicationResponse(
    Long id,
    String applicationNo,
    String applicantId,
    String applicantName,
    String applicantCip,
    BigDecimal requestedAmount,
    Integer termMonths,
    BigDecimal interestRate,
    InterestType interestType,
    String purpose,
    LoanStatus status,
    Integer creditScore,
    String officerId,
    String branchCode,
    String activeMqMessageId,
    LocalDateTime createdAt,
    String message
) {}
