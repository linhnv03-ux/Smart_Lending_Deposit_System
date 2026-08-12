package com.bank.slds.adapter.dto;

import com.bank.slds.adapter.model.JournalType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PostingResponse(
    boolean success,
    String journalRef,
    JournalType journalType,
    String debitAccount,
    String creditAccount,
    BigDecimal amount,
    String coreBankingStatus,
    String circuitBreakerState,
    boolean fallbackTriggered,
    LocalDateTime postedAt,
    String message
) {}
