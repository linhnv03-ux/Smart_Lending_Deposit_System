package com.bank.slds.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    /**
     * KeyResolver to resolve Redis Rate Limiter Key based on Client IP or Authorization Header
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return Mono.just(authHeader.substring(7)); // Rate limit per JWT user token
            }
            // Fallback to client remote address IP
            String ip = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "127.0.0.1";
            return Mono.just(ip);
        };
    }
}
