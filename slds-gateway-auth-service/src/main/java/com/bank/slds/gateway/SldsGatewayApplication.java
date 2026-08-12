package com.bank.slds.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * SLDS Microservice 1: API Gateway & Authentication Service
 * Routing, JWT OAuth2 Verification, Redis Session Management & Rate Limiting (DDoS Protection)
 */
@SpringBootApplication(exclude = {
    R2dbcAutoConfiguration.class,
    R2dbcDataAutoConfiguration.class,
    R2dbcRepositoriesAutoConfiguration.class
})
@EnableJpaRepositories(basePackages = "com.bank.slds.gateway.repository")
@EntityScan(basePackages = "com.bank.slds.gateway.model")
public class SldsGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SldsGatewayApplication.class, args);
    }
}
