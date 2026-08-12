package com.bank.slds.loan.dto;

import com.bank.slds.loan.model.InterestType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record LoanApplicationRequest(
    @NotBlank(message = "Applicant ID is required")
    String applicantId,

    @NotBlank(message = "Applicant Name is required")
    String applicantName,

    @NotBlank(message = "Applicant CIP (Citizen ID) is required")
    String applicantCip,

    @NotNull(message = "Requested Amount is required")
    @Positive(message = "Amount must be positive")
    BigDecimal requestedAmount,

    @NotNull(message = "Term in months is required")
    @Min(value = 1, message = "Minimum term is 1 month")
    @Max(value = 360, message = "Maximum term is 360 months")
    Integer termMonths,

    @NotNull(message = "Interest Rate is required")
    @Positive(message = "Rate must be positive")
    BigDecimal interestRate,

    @NotNull(message = "Interest Type is required")
    InterestType interestType,

    String purpose,
    String officerId,
    String branchCode
) {}
