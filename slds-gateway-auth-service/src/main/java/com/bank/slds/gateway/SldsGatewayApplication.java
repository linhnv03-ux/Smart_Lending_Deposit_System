package com.bank.slds.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SLDS Microservice 1: API Gateway & Authentication Service
 * Routing, JWT OAuth2 Verification, Redis Session Management & Rate Limiting (DDoS Protection)
 */
@SpringBootApplication
public class SldsGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SldsGatewayApplication.class, args);
    }
}
