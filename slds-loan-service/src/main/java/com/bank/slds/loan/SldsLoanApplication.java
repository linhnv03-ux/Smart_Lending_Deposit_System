package com.bank.slds.loan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.jms.annotation.EnableJms;

/**
 * SLDS Microservice 2: Loan Service
 * Handles Loan Lifecycle: Applications, Strategy-Based Interest Calculations, Repayment Schedules,
 * Disbursement, Repayments, ActiveMQ Assessment Event Publishing, and Elasticsearch Auditing.
 */
@SpringBootApplication
@EnableCaching
@EnableJms
public class SldsLoanApplication {

    public static void main(String[] args) {
        SpringApplication.run(SldsLoanApplication.class, args);
    }
}
