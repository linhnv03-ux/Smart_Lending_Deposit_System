package com.bank.slds.adapter.service;

import com.bank.slds.adapter.dto.*;
import com.bank.slds.adapter.model.*;
import com.bank.slds.adapter.repository.CoreBankingJournalRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoreBankingExecutionService {

    private final CoreBankingJournalRepository journalRepository;
    private final AtomicBoolean simulateFailure = new AtomicBoolean(false);

    public void setSimulateFailure(boolean fail) {
        this.simulateFailure.set(fail);
        log.warn("Simulate Core Banking Oracle DB timeout state set to: {}", fail);
    }

    public boolean isSimulateFailure() {
        return this.simulateFailure.get();
    }

    @CircuitBreaker(name = "coreBankingAdapter", fallbackMethod = "fallbackPosting")
    @Retry(name = "coreBankingAdapter")
    @Transactional
    public PostingResponse postJournal(PostingRequest request) {
        if (simulateFailure.get()) {
            log.error("Core Banking Oracle DB timeout simulation triggered!");
            throw new RuntimeException("Core Banking System Offline / Oracle DB Timeout Exception");
        }

        String journalRef = "CB-JRN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        CoreBankingJournal entity = CoreBankingJournal.builder()
            .journalRef(journalRef)
            .journalType(request.journalType())
            .debitAccount(request.debitAccount())
            .creditAccount(request.creditAccount())
            .amount(request.amount())
            .currency("VND")
            .status("SUCCESS")
            .responseMessage("Hạch toán bút toán Core Banking Oracle DB thành công")
            .postedAt(LocalDateTime.now())
            .build();

        journalRepository.save(entity);

        log.info("Core Banking journal posted successfully: Ref={}, Type={}, Debit={}, Credit={}, Amount={}",
            journalRef, request.journalType(), request.debitAccount(), request.creditAccount(), request.amount());

        return new PostingResponse(
            true,
            journalRef,
            request.journalType(),
            request.debitAccount(),
            request.creditAccount(),
            request.amount(),
            "SUCCESS",
            "CLOSED (Healthy)",
            false,
            LocalDateTime.now(),
            "Hạch toán bút toán Core Banking thành công"
        );
    }

    public PostingResponse fallbackPosting(PostingRequest request, Throwable t) {
        String fallbackJournalRef = "FALLBACK-QUEUE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.warn("Resilience4j CIRCUIT BREAKER TRIGGERED for journal posting. Reason: {}. Fallback Ref: {}",
            t.getMessage(), fallbackJournalRef);

        return new PostingResponse(
            false,
            fallbackJournalRef,
            request.journalType(),
            request.debitAccount(),
            request.creditAccount(),
            request.amount(),
            "QUEUED_OFFLINE",
            "OPEN (Fallback Active)",
            true,
            LocalDateTime.now(),
            "Circuit Breaker kích hoạt: Core Banking chập chờn. Bút toán đã được đưa vào Offline Fallback Queue để xử lý lại khi Core Banking phục hồi."
        );
    }

    public AccountBalanceResponse getAccountBalance(String accountNumber) {
        return new AccountBalanceResponse(
            accountNumber,
            "Tài khoản Khách hàng " + accountNumber,
            accountNumber.startsWith("STK") ? "DEPOSIT_ACCOUNT" : (accountNumber.startsWith("HDTD") ? "LOAN_ACCOUNT" : "PAYMENT_ACCOUNT"),
            new BigDecimal("1500000000"),
            new BigDecimal("1500000000"),
            "VND",
            "ACTIVE",
            LocalDateTime.now()
        );
    }
}
