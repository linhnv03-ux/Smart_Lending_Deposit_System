package com.bank.slds.deposit.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CloseDepositResponse(
    boolean success,
    String receiptNo,
    String accountNumber,
    String customerId,
    BigDecimal principalPaid,
    BigDecimal interestPaid,
    BigDecimal totalPayout,
    boolean isEarlySettlement, // Tất toán trước hạn hay đúng hạn
    BigDecimal appliedInterestRate,
    LocalDateTime closedAt,
    String destinationAccount,
    String status,
    String message
) {}
