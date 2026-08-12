package com.bank.slds.assessment.service;

import com.bank.slds.assessment.dto.CreditAssessmentMessage;
import com.bank.slds.assessment.dto.CreditEvaluationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActiveMqAssessmentConsumer {

    private final CreditScoringService creditScoringService;

    @JmsListener(destination = "loan.application.assessment")
    public void consumeAssessmentEvent(CreditAssessmentMessage message) {
        log.info("Received ActiveMQ Credit Assessment Event for ApplicationNo: {}, Applicant: {}",
            message.applicationNo(), message.applicantName());

        try {
            CreditEvaluationResult result = creditScoringService.evaluateCredit(message);
            log.info("Async Background Assessment Completed for ApplicationNo: {} -> Decision: {}, Score: {}",
                message.applicationNo(), result.decisionStatus(), result.creditScore());
        } catch (Exception e) {
            log.error("Failed to process Credit Assessment Event for ApplicationNo: {}: {}",
                message.applicationNo(), e.getMessage());
        }
    }
}
