package com.bank.slds.loan.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class CoreBankingAdapterService {

    private final AtomicBoolean simulateFailure = new AtomicBoolean(false);

    public void setSimulateFailure(boolean fail) {
        this.simulateFailure.set(fail);
    }

    public boolean isSimulateFailure() {
        return this.simulateFailure.get();
    }

    @CircuitBreaker(name = "coreBankingAdapter", fallbackMethod = "fallbackDisburseToCoreBanking")
    @Retry(name = "coreBankingAdapter")
    public String executeDisbursementJournal(String contractNo, BigDecimal amount, String destinationAccount) {
        if (simulateFailure.get()) {
            log.error("Core Banking Oracle DB timeout simulation triggered!");
            throw new RuntimeException("Core Banking System Offline / Timeout Exception");
        }

        String journalRef = "CB-JRN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("Core Banking journal created successfully: Ref={}, Amount={}, Account={}",
            journalRef, amount, destinationAccount);
        return journalRef;
    }

    public String fallbackDisburseToCoreBanking(String contractNo, BigDecimal amount, String destinationAccount, Throwable t) {
        String fallbackJournalRef = "FALLBACK-QUEUE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.warn("Resilience4j CIRCUIT BREAKER TRIGGERED for contract {}. Caused by: {}. Handled via Fallback Ref: {}",
            contractNo, t.getMessage(), fallbackJournalRef);
        return fallbackJournalRef;
    }
}
