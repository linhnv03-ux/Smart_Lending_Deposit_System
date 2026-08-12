package com.bank.slds.loan.controller;

import com.bank.slds.loan.constant.ApiPath;
import com.bank.slds.loan.dto.LoanApplicationRequest;
import com.bank.slds.loan.dto.LoanApplicationResponse;
import com.bank.slds.loan.service.LoanApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPath.Loans.BASE + ApiPath.Loans.APPLICATIONS)
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationController {

    private final LoanApplicationService applicationService;

    @PostMapping
    public ResponseEntity<LoanApplicationResponse> createLoanApplication(@Valid @RequestBody LoanApplicationRequest request) {
        log.info("REST Request to create new Loan Application for CIP: {}", request.applicantCip());
        LoanApplicationResponse response = applicationService.createApplication(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{applicationNo}")
    public ResponseEntity<LoanApplicationResponse> getLoanApplication(@PathVariable String applicationNo) {
        return ResponseEntity.ok(applicationService.getApplication(applicationNo));
    }
}
