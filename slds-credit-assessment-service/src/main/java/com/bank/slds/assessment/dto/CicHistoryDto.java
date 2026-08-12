package com.bank.slds.assessment.dto;

import com.bank.slds.assessment.model.DebtGroup;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CicHistoryDto(
    String applicantCip,
    String fullName,
    DebtGroup currentDebtGroup,
    int activeCreditContracts,
    BigDecimal totalOutstandingBalance,
    BigDecimal maxOverdueDays,
    boolean hasBadDebtIn5Years,
    LocalDate lastCicQueryDate
) {}
