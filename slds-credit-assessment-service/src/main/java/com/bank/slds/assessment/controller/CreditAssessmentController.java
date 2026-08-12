package com.bank.slds.assessment.controller;

import com.bank.slds.assessment.dto.*;
import com.bank.slds.assessment.service.CreditScoringService;
import com.bank.slds.assessment.service.ElasticsearchAssessmentLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/assessment")
@RequiredArgsConstructor
@Slf4j
public class CreditAssessmentController {

    private final CreditScoringService creditScoringService;
    private final ElasticsearchAssessmentLogService esLogService;

    @PostMapping("/evaluate")
    public ResponseEntity<CreditEvaluationResult> evaluateCredit(@RequestBody CreditAssessmentMessage message) {
        log.info("REST Request for Synchronous Credit Assessment on ApplicationNo: {}", message.applicationNo());
        return ResponseEntity.ok(creditScoringService.evaluateCredit(message));
    }

    @GetMapping("/cic/{applicantCip}")
    public ResponseEntity<CicHistoryDto> queryCicHistory(@PathVariable String applicantCip) {
        log.info("REST Request for CIC Bad Debt Query on CIP: {}", applicantCip);
        return ResponseEntity.ok(creditScoringService.queryCicHistory(applicantCip));
    }

    @GetMapping("/logs")
    public ResponseEntity<List<Map<String, Object>>> searchCreditLogs(@RequestParam(required = false) String query) {
        log.info("REST Request to Elasticsearch for Credit Assessment Audit Search query: {}", query);
        return ResponseEntity.ok(esLogService.searchLogs(query));
    }
}
