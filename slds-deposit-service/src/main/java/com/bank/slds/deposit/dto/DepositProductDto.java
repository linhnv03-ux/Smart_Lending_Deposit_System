package com.bank.slds.deposit.dto;

import com.bank.slds.deposit.model.InterestPayoutType;

import java.io.Serializable;
import java.math.BigDecimal;

public record DepositProductDto(
    String productCode,
    String productName,
    Integer termMonths,
    BigDecimal interestRateAnnual,
    InterestPayoutType payoutType,
    BigDecimal minimumDepositAmount,
    boolean active
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
