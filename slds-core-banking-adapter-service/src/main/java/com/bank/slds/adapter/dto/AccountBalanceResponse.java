package com.bank.slds.adapter.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountBalanceResponse(
    String accountNumber,
    String accountName,
    String accountType, // PAYMENT_ACCOUNT, LOAN_ACCOUNT, DEPOSIT_ACCOUNT
    BigDecimal currentBalance,
    BigDecimal availableBalance,
    String currency,
    String status,
    LocalDateTime queriedAt
) {}
