package com.bank.slds.loan.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreditAssessmentMessage(
    String applicationNo,
    String applicantId,
    String applicantName,
    String applicantCip,
    BigDecimal requestedAmount,
    Integer termMonths,
    LocalDateTime eventTimestamp
) implements Serializable {}
