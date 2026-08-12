package com.bank.slds.loan.service;

import com.bank.slds.loan.dto.CreditAssessmentMessage;
import com.bank.slds.loan.model.LoanStatus;
import com.bank.slds.loan.repository.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActiveMqConsumerService {

    private final LoanApplicationRepository applicationRepository;

    @JmsListener(destination = ActiveMqProducerService.ASSESSMENT_QUEUE)
    public void processCreditAssessment(CreditAssessmentMessage message) {
        log.info("Received ActiveMQ Assessment Message for ApplicationNo: {}", message.applicationNo());

        applicationRepository.findByApplicationNo(message.applicationNo()).ifPresent(app -> {
            // Simulate CIC Credit Scoring
            int creditScore = 720 + (Math.abs(message.applicantCip().hashCode()) % 130);
            app.setCreditScore(creditScore);

            if (creditScore >= 680) {
                app.setStatus(LoanStatus.APPROVED);
                log.info("Loan Application {} APPROVED with Credit Score: {}", message.applicationNo(), creditScore);
            } else {
                app.setStatus(LoanStatus.REJECTED);
                log.info("Loan Application {} REJECTED due to low Credit Score: {}", message.applicationNo(), creditScore);
            }
            applicationRepository.save(app);
        });
    }
}
