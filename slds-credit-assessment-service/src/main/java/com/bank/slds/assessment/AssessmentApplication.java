package com.bank.slds.assessment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableJms
public class AssessmentApplication {
    public static void main(String[] args) {
        SpringApplication.run(AssessmentApplication.class, args);
    }
}
