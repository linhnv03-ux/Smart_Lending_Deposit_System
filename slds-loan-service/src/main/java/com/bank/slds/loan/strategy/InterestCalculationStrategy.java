package com.bank.slds.loan.strategy;

import com.bank.slds.loan.dto.RepaymentScheduleDto;

import java.math.BigDecimal;
import java.util.List;

public interface InterestCalculationStrategy {

    /**
     * Calculates full repayment schedule for given loan parameters
     */
    List<RepaymentScheduleDto> calculateSchedule(BigDecimal amount, BigDecimal annualRate, int termMonths);
}
