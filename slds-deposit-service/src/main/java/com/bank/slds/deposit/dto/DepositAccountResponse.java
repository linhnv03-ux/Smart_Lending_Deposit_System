package com.bank.slds.deposit.dto;

import com.bank.slds.deposit.model.InterestPayoutType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DepositAccountResponse(
    boolean success,
    String accountNumber,
    String customerId,
    String customerName,
    String productCode,
    String productName,
    BigDecimal principalAmount,
    BigDecimal interestRateAnnual,
    Integer termMonths,
    InterestPayoutType payoutType,
    LocalDate openDate,
    LocalDate maturityDate,
    BigDecimal estimatedInterestAtMaturity,
    BigDecimal totalMaturityAmount,
    String status,
    String message
) {}
