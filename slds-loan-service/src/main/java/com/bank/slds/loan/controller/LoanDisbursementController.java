package com.bank.slds.loan.controller;

import com.bank.slds.loan.constant.ApiPath;
import com.bank.slds.loan.dto.*;
import com.bank.slds.loan.factory.InterestStrategyFactory;
import com.bank.slds.loan.model.InterestType;
import com.bank.slds.loan.service.*;
import com.bank.slds.loan.strategy.InterestCalculationStrategy;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApiPath.Loans.BASE)
@RequiredArgsConstructor
@Slf4j
public class LoanDisbursementController {

    private final LoanDisbursementService disbursementService;
    private final LoanRepaymentService repaymentService;
    private final InterestStrategyFactory strategyFactory;
    private final CoreBankingAdapterService coreBankingAdapter;
    private final ElasticsearchAuditService auditService;

    @PostMapping(ApiPath.Loans.DISBURSE)
    public ResponseEntity<DisbursementResponse> disburseLoan(@Valid @RequestBody DisbursementRequest request) {
        log.info("REST Request for Loan Disbursement on ApplicationNo: {}", request.applicationNo());
        DisbursementResponse response = disbursementService.disburseLoan(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(ApiPath.Loans.REPAY)
    public ResponseEntity<RepaymentResponse> processRepayment(@Valid @RequestBody RepaymentRequest request) {
        log.info("REST Request for Loan Repayment on ContractNo: {}", request.contractNo());
        RepaymentResponse response = repaymentService.processRepayment(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping(ApiPath.Loans.SCHEDULES_PREVIEW)
    public ResponseEntity<List<RepaymentScheduleDto>> previewRepaymentSchedule(
            @RequestParam BigDecimal amount,
            @RequestParam BigDecimal rate,
            @RequestParam int termMonths,
            @RequestParam(defaultValue = "REDUCING_BALANCE") InterestType interestType) {

        InterestCalculationStrategy strategy = strategyFactory.getStrategy(interestType);
        List<RepaymentScheduleDto> schedule = strategy.calculateSchedule(amount, rate, termMonths);
        return ResponseEntity.ok(schedule);
    }

    @GetMapping(ApiPath.Loans.CIRCUIT_BREAKER_STATUS)
    public ResponseEntity<Map<String, Object>> getCircuitBreakerStatus() {
        return ResponseEntity.ok(Map.of(
            "service", "CoreBankingAdapterService",
            "state", coreBankingAdapter.isSimulateFailure() ? "OPEN (Simulated Failure)" : "CLOSED (Healthy)",
            "simulatedFailure", coreBankingAdapter.isSimulateFailure()
        ));
    }

    @PostMapping(ApiPath.Loans.CIRCUIT_BREAKER_TOGGLE)
    public ResponseEntity<Map<String, Object>> toggleCircuitBreaker(@RequestBody Map<String, Boolean> body) {
        boolean simulateError = body.getOrDefault("simulateError", true);
        coreBankingAdapter.setSimulateFailure(simulateError);
        return ResponseEntity.ok(Map.of(
            "message", simulateError ? "Core Banking set to FAILURE. Circuit Breaker OPEN." : "Core Banking connection healthy. Circuit Breaker CLOSED.",
            "circuitBreakerState", simulateError ? "OPEN" : "CLOSED"
        ));
    }

    @GetMapping(ApiPath.Loans.AUDIT_LOGS)
    public ResponseEntity<List<AuditLogDto>> getAuditLogs(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(auditService.getAuditLogs(query));
    }
}
