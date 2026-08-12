package com.bank.slds.loan.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RepaymentResponse(
    boolean success,
    String receiptNo,
    String contractNo,
    BigDecimal paidAmount,
    BigDecimal principalPaid,
    BigDecimal interestPaid,
    BigDecimal remainingBalance,
    String status,
    LocalDateTime paidAt,
    String message
) {}
