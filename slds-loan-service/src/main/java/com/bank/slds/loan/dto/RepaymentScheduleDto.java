package com.bank.slds.loan.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RepaymentScheduleDto(
    Integer period,
    LocalDate dueDate,
    BigDecimal principalPayable,
    BigDecimal interestPayable,
    BigDecimal totalInstallment,
    BigDecimal remainingBalance
) {}
