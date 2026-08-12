package com.bank.slds.loan.dto;

import com.bank.slds.loan.model.InterestType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record DisbursementResponse(
    boolean success,
    String disbursementId,
    String loanContractNo,
    String status,
    String applicantId,
    String applicantName,
    BigDecimal disbursedAmount,
    BigDecimal interestRate,
    InterestType interestType,
    Integer termMonths,
    BigDecimal monthlyInstallment,
    BigDecimal totalInterest,
    BigDecimal totalRepayment,
    LocalDateTime disbursementDate,
    String disbursementAccount,
    String coreBankingJournalRef,
    long processingTimeMs,
    String redisCacheStatus,
    String circuitBreakerState,
    String activeMqMessageId,
    String elasticsearchAuditId,
    List<RepaymentScheduleDto> repaymentSchedule,
    boolean fallbackTriggered,
    String fallbackReason
) {}
