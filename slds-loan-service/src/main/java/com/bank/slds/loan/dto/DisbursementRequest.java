package com.bank.slds.loan.dto;

import com.bank.slds.loan.model.InterestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DisbursementRequest(
    @NotBlank(message = "Application No or Contract No is required")
    String applicationNo,

    @NotBlank(message = "Disbursement Account is required")
    String disbursementAccount,

    @NotNull(message = "Loan Amount is required")
    @Positive
    BigDecimal loanAmount,

    @NotNull(message = "Term Months is required")
    Integer termMonths,

    @NotNull(message = "Interest Rate is required")
    BigDecimal interestRate,

    @NotNull(message = "Interest Type is required")
    InterestType interestType,

    String disbursementChannel,
    String officerId,
    String branchCode
) {}
