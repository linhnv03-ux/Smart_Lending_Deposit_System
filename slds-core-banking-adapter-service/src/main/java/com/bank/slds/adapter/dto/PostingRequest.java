package com.bank.slds.adapter.dto;

import com.bank.slds.adapter.model.JournalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PostingRequest(
    @NotNull(message = "Journal Type is required")
    JournalType journalType,

    @NotBlank(message = "Debit Account is required")
    String debitAccount,

    @NotBlank(message = "Credit Account is required")
    String creditAccount,

    @NotNull(message = "Amount is required")
    @Positive
    BigDecimal amount,

    String narrative,
    String referenceNo
) {}
