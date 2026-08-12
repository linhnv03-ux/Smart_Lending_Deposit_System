package com.bank.slds.loan.service;

import com.bank.slds.loan.dto.CreditAssessmentMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActiveMqProducerService {

    private final JmsTemplate jmsTemplate;
    public static final String ASSESSMENT_QUEUE = "loan.application.assessment";

    public String publishCreditAssessmentEvent(CreditAssessmentMessage message) {
        String eventId = "AMQ-EVT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        try {
            jmsTemplate.convertAndSend(ASSESSMENT_QUEUE, message);
            log.info("Published Credit Assessment event to ActiveMQ Queue '{}': ApplicationNo={}", 
                ASSESSMENT_QUEUE, message.applicationNo());
            return eventId;
        } catch (Exception e) {
            log.error("Failed to publish event to ActiveMQ: {}", e.getMessage());
            return "AMQ-OFFLINE-FALLBACK-" + eventId;
        }
    }
}
