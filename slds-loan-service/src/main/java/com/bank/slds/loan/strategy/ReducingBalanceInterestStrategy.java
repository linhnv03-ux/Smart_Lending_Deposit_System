package com.bank.slds.loan.strategy;

import com.bank.slds.loan.dto.RepaymentScheduleDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Reducing Balance Strategy: Lãi suất tính theo dư nợ thực tế giảm dần
 */
@Component("REDUCING_BALANCE")
public class ReducingBalanceInterestStrategy implements InterestCalculationStrategy {

    @Override
    public List<RepaymentScheduleDto> calculateSchedule(BigDecimal amount, BigDecimal annualRate, int termMonths) {
        List<RepaymentScheduleDto> schedule = new ArrayList<>();
        BigDecimal term = BigDecimal.valueOf(termMonths);

        BigDecimal monthlyPrincipal = amount.divide(term, 2, RoundingMode.HALF_UP);
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
            .divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);

        BigDecimal remainingBalance = amount;
        LocalDate now = LocalDate.now();

        for (int period = 1; period <= termMonths; period++) {
            BigDecimal monthlyInterest = remainingBalance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal monthlyInstallment = monthlyPrincipal.add(monthlyInterest);

            if (period == termMonths) {
                remainingBalance = BigDecimal.ZERO;
            } else {
                remainingBalance = remainingBalance.subtract(monthlyPrincipal);
            }

            schedule.add(new RepaymentScheduleDto(
                period,
                now.plusMonths(period),
                monthlyPrincipal,
                monthlyInterest,
                monthlyInstallment,
                remainingBalance.max(BigDecimal.ZERO)
            ));
        }

        return schedule;
    }
}
