package com.bank.slds.deposit.dto;

import jakarta.validation.constraints.NotBlank;

public record CloseDepositRequest(
    @NotBlank(message = "Account Number is required")
    String accountNumber,

    @NotBlank(message = "Destination Account is required")
    String destinationAccount,

    String tellerId,
    String branchCode
) {}
