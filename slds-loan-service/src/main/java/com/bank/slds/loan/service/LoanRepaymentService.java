package com.bank.slds.loan.service;

import com.bank.slds.loan.dto.RepaymentRequest;
import com.bank.slds.loan.dto.RepaymentResponse;
import com.bank.slds.loan.model.LoanContract;
import com.bank.slds.loan.model.LoanStatus;
import com.bank.slds.loan.model.RepaymentSchedule;
import com.bank.slds.loan.repository.LoanContractRepository;
import com.bank.slds.loan.repository.RepaymentScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanRepaymentService {

    private final LoanContractRepository contractRepository;
    private final RepaymentScheduleRepository scheduleRepository;
    private final ElasticsearchAuditService auditService;

    @Transactional
    public RepaymentResponse processRepayment(RepaymentRequest request) {
        long startTime = System.currentTimeMillis();

        LoanContract contract = contractRepository.findByContractNo(request.contractNo())
            .orElseThrow(() -> new IllegalArgumentException("Loan Contract not found: " + request.contractNo()));

        List<RepaymentSchedule> schedules = scheduleRepository.findByContractNoOrderByPeriodAsc(request.contractNo());

        BigDecimal remainingPayment = request.repaymentAmount();
        BigDecimal totalPrincipalPaid = BigDecimal.ZERO;
        BigDecimal totalInterestPaid = BigDecimal.ZERO;

        for (RepaymentSchedule schedule : schedules) {
            if (remainingPayment.compareTo(BigDecimal.ZERO) <= 0) break;
            if (schedule.isPaid()) continue;

            BigDecimal installment = schedule.getTotalInstallment();
            if (remainingPayment.compareTo(installment) >= 0) {
                schedule.setPaid(true);
                schedule.setPaidDate(LocalDate.now());
                totalPrincipalPaid = totalPrincipalPaid.add(schedule.getPrincipalPayable());
                totalInterestPaid = totalInterestPaid.add(schedule.getInterestPayable());
                remainingPayment = remainingPayment.subtract(installment);
            }
        }

        BigDecimal newBalance = contract.getCurrentBalance().subtract(totalPrincipalPaid).max(BigDecimal.ZERO);
        contract.setCurrentBalance(newBalance);

        if (newBalance.compareTo(BigDecimal.ZERO) == 0) {
            contract.setStatus(LoanStatus.CLOSED);
        } else {
            contract.setStatus(LoanStatus.REPAYING);
        }

        contractRepository.save(contract);
        scheduleRepository.saveAll(schedules);

        String receiptNo = "REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        long duration = System.currentTimeMillis() - startTime;
        auditService.recordAudit("REPAY_LOAN", request.tellerId(), request.contractNo(),
            "Paid " + request.repaymentAmount() + " VND for contract " + request.contractNo(), "SUCCESS", duration);

        return new RepaymentResponse(
            true,
            receiptNo,
            request.contractNo(),
            request.repaymentAmount(),
            totalPrincipalPaid,
            totalInterestPaid,
            newBalance,
            contract.getStatus().name(),
            LocalDateTime.now(),
            newBalance.compareTo(BigDecimal.ZERO) == 0 ? "Khoản vay đã được tất toán hoàn toàn" : "Thu nợ kỳ thành công"
        );
    }
}
