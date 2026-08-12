package com.bank.slds.deposit.dto;

import com.bank.slds.deposit.model.InterestPayoutType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OpenDepositRequest(
    @NotBlank(message = "Customer ID is required")
    String customerId,

    @NotBlank(message = "Customer Name is required")
    String customerName,

    @NotBlank(message = "Product Code is required")
    String productCode,

    @NotNull(message = "Deposit Amount is required")
    @Positive
    BigDecimal depositAmount,

    Integer termMonths,

    BigDecimal interestRate,

    InterestPayoutType payoutType,

    String fundingAccount,

    String branchCode
) {}
