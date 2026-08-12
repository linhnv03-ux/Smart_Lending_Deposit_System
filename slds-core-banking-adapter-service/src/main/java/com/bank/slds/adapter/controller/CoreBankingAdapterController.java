package com.bank.slds.adapter.controller;

import com.bank.slds.adapter.constant.ApiPath;
import com.bank.slds.adapter.dto.*;
import com.bank.slds.adapter.service.CoreBankingExecutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(ApiPath.CoreBanking.BASE)
@RequiredArgsConstructor
@Slf4j
public class CoreBankingAdapterController {

    private final CoreBankingExecutionService executionService;

    @PostMapping(ApiPath.CoreBanking.JOURNALS_POST)
    public ResponseEntity<PostingResponse> postJournal(@Valid @RequestBody PostingRequest request) {
        log.info("REST Request to post Core Banking Journal: Type={}, Debit={}, Credit={}, Amount={}",
            request.journalType(), request.debitAccount(), request.creditAccount(), request.amount());
        return ResponseEntity.ok(executionService.postJournal(request));
    }

    @GetMapping(ApiPath.CoreBanking.ACCOUNT_BALANCE)
    public ResponseEntity<AccountBalanceResponse> getAccountBalance(@PathVariable String accountNumber) {
        log.info("REST Request to query Core Banking balance for account: {}", accountNumber);
        return ResponseEntity.ok(executionService.getAccountBalance(accountNumber));
    }

    @GetMapping(ApiPath.CoreBanking.CIRCUIT_BREAKER_STATUS)
    public ResponseEntity<Map<String, Object>> getCircuitBreakerStatus() {
        return ResponseEntity.ok(Map.of(
            "service", "CoreBankingAdapterService",
            "state", executionService.isSimulateFailure() ? "OPEN (Simulated Failure)" : "CLOSED (Healthy)",
            "simulatedFailure", executionService.isSimulateFailure()
        ));
    }

    @PostMapping(ApiPath.CoreBanking.CIRCUIT_BREAKER_TOGGLE)
    public ResponseEntity<Map<String, Object>> toggleCircuitBreaker(@RequestBody Map<String, Boolean> body) {
        boolean simulateError = body.getOrDefault("simulateError", true);
        executionService.setSimulateFailure(simulateError);
        return ResponseEntity.ok(Map.of(
            "message", simulateError ? "Core Banking Oracle DB timeout simulation enabled. Circuit Breaker is OPEN." : "Core Banking connection healthy. Circuit Breaker is CLOSED.",
            "circuitBreakerState", simulateError ? "OPEN" : "CLOSED"
        ));
    }
}
