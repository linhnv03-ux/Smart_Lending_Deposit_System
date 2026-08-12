package com.bank.slds.loan.service;

import com.bank.slds.loan.dto.DisbursementRequest;
import com.bank.slds.loan.dto.DisbursementResponse;
import com.bank.slds.loan.dto.RepaymentScheduleDto;
import com.bank.slds.loan.factory.InterestStrategyFactory;
import com.bank.slds.loan.model.*;
import com.bank.slds.loan.repository.*;
import com.bank.slds.loan.strategy.InterestCalculationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanDisbursementService {

    private final LoanContractRepository contractRepository;
    private final DisbursementTransactionRepository transactionRepository;
    private final RepaymentScheduleRepository scheduleRepository;
    private final InterestStrategyFactory strategyFactory;
    private final CoreBankingAdapterService coreBankingAdapter;
    private final ElasticsearchAuditService auditService;

    @Transactional
    public DisbursementResponse disburseLoan(DisbursementRequest request) {
        long startTime = System.currentTimeMillis();

        String contractNo = "HDTD-" + (10000000 + (int)(Math.random() * 90000000));
        String disbursementId = "DISB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 1. Calculate schedule using Strategy Pattern
        InterestCalculationStrategy strategy = strategyFactory.getStrategy(request.interestType());
        List<RepaymentScheduleDto> scheduleDtos = strategy.calculateSchedule(
            request.loanAmount(), request.interestRate(), request.termMonths());

        BigDecimal totalInterest = scheduleDtos.stream()
            .map(RepaymentScheduleDto::interestPayable)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Call Core Banking with Resilience4j Circuit Breaker
        String journalRef;
        boolean fallbackTriggered = false;
        String fallbackReason = null;

        try {
            journalRef = coreBankingAdapter.executeDisbursementJournal(contractNo, request.loanAmount(), request.disbursementAccount());
            if (journalRef.startsWith("FALLBACK")) {
                fallbackTriggered = true;
                fallbackReason = "Resilience4j Circuit Breaker OPEN: Core Banking Oracle DB timeout. Handled via fallback queue.";
            }
        } catch (Exception e) {
            fallbackTriggered = true;
            journalRef = "FALLBACK-QUEUE-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            fallbackReason = "Circuit Breaker OPEN: " + e.getMessage();
        }

        // 3. Save Contract and Schedules
        LoanContract contract = LoanContract.builder()
            .contractNo(contractNo)
            .applicationNo(request.applicationNo())
            .applicantId("CUST-" + Math.abs(request.disbursementAccount().hashCode()))
            .applicantName("Khách hàng Vay " + contractNo)
            .principalAmount(request.loanAmount())
            .currentBalance(request.loanAmount())
            .interestRate(request.interestRate())
            .interestType(request.interestType())
            .termMonths(request.termMonths())
            .status(LoanStatus.DISBURSED)
            .disbursementAccount(request.disbursementAccount())
            .disbursedAt(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .build();
        contractRepository.save(contract);

        // Save Disbursement Transaction
        DisbursementTransaction txn = DisbursementTransaction.builder()
            .transactionId(disbursementId)
            .contractNo(contractNo)
            .amount(request.loanAmount())
            .destinationAccount(request.disbursementAccount())
            .coreBankingJournalRef(journalRef)
            .status(fallbackTriggered ? "QUEUED_OFFLINE" : "SUCCESS")
            .channel(request.disbursementChannel() != null ? request.disbursementChannel() : "INTERNET_BANKING")
            .createdAt(LocalDateTime.now())
            .build();
        transactionRepository.save(txn);

        // Save Schedule
        scheduleDtos.forEach(s -> {
            RepaymentSchedule entity = RepaymentSchedule.builder()
                .contractNo(contractNo)
                .period(s.period())
                .dueDate(s.dueDate())
                .principalPayable(s.principalPayable())
                .interestPayable(s.interestPayable())
                .totalInstallment(s.totalInstallment())
                .remainingBalance(s.remainingBalance())
                .isPaid(false)
                .build();
            scheduleRepository.save(entity);
        });

        long executionTimeMs = System.currentTimeMillis() - startTime;
        String auditId = auditService.recordAudit("DISBURSE_LOAN", request.officerId(), contractNo,
            "Disbursed " + request.loanAmount() + " to account " + request.disbursementAccount(),
            fallbackTriggered ? "WARNING" : "SUCCESS", executionTimeMs);

        return new DisbursementResponse(
            true,
            disbursementId,
            contractNo,
            "DISBURSED",
            contract.getApplicantId(),
            contract.getApplicantName(),
            request.loanAmount(),
            request.interestRate(),
            request.interestType(),
            request.termMonths(),
            scheduleDtos.isEmpty() ? BigDecimal.ZERO : scheduleDtos.get(0).totalInstallment(),
            totalInterest,
            request.loanAmount().add(totalInterest),
            LocalDateTime.now(),
            request.disbursementAccount(),
            journalRef,
            executionTimeMs,
            "HIT",
            fallbackTriggered ? "OPEN (Fallback Active)" : "CLOSED (Healthy)",
            "AMQ-MSG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
            auditId,
            scheduleDtos,
            fallbackTriggered,
            fallbackReason
        );
    }
}
