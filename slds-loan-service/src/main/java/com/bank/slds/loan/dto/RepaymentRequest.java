package com.bank.slds.loan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RepaymentRequest(
    @NotBlank(message = "Contract No is required")
    String contractNo,

    @NotNull(message = "Repayment Amount is required")
    @Positive
    BigDecimal repaymentAmount,

    @NotBlank(message = "Source Account is required")
    String sourceAccount,

    String tellerId,
    String branchCode
) {}
