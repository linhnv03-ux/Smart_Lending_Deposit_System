package com.bank.slds.deposit.controller;

import com.bank.slds.deposit.dto.*;
import com.bank.slds.deposit.model.DepositAccount;
import com.bank.slds.deposit.service.DepositService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/deposits")
@RequiredArgsConstructor
@Slf4j
public class DepositController {

    private final DepositService depositService;

    @GetMapping("/products")
    public ResponseEntity<List<DepositProductDto>> getDepositProducts() {
        return ResponseEntity.ok(depositService.getActiveProducts());
    }

    @PostMapping("/schedules/calculate-interest")
    public ResponseEntity<CalculateInterestResponse> calculateInterest(@RequestBody CalculateInterestRequest request) {
        return ResponseEntity.ok(depositService.calculateInterest(request));
    }

    @PostMapping("/accounts/open")
    public ResponseEntity<DepositAccountResponse> openDepositAccount(@Valid @RequestBody OpenDepositRequest request) {
        log.info("REST Request to open deposit account for customer: {}", request.customerName());
        return ResponseEntity.ok(depositService.openDepositAccount(request));
    }

    @PostMapping("/accounts/close")
    public ResponseEntity<CloseDepositResponse> closeDepositAccount(@Valid @RequestBody CloseDepositRequest request) {
        log.info("REST Request to close deposit account STK: {}", request.accountNumber());
        return ResponseEntity.ok(depositService.closeDepositAccount(request));
    }

    @GetMapping("/accounts/customer/{customerId}")
    public ResponseEntity<List<DepositAccount>> getCustomerAccounts(@PathVariable String customerId) {
        return ResponseEntity.ok(depositService.getAccountsByCustomer(customerId));
    }
}
