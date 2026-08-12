package com.bank.slds.deposit.dto;

import java.math.BigDecimal;

public record CalculateInterestResponse(
    BigDecimal principal,
    BigDecimal annualRate,
    int termMonths,
    BigDecimal fullTermInterest,
    BigDecimal totalPayoutAtMaturity,
    BigDecimal dailyInterestAmount,
    BigDecimal earlyWithdrawalInterest, // Lãi suất phạt tất toán trước hạn (ví dụ: lãi không kỳ hạn 0.2%/năm)
    String formula
) {}
