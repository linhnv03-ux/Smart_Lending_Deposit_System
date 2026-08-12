package com.bank.slds.deposit.dto;

import com.bank.slds.deposit.model.InterestPayoutType;

import java.math.BigDecimal;

public record CalculateInterestRequest(
    BigDecimal principal,
    BigDecimal annualRate,
    int termMonths,
    int actualDaysHeld,
    InterestPayoutType payoutType
) {}
