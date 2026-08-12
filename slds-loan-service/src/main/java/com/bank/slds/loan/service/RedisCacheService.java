package com.bank.slds.loan.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;

@Service
@Slf4j
public class RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheService(@Autowired(required = false) RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public BigDecimal getCachedInterestRate(String productKey, BigDecimal defaultRate) {
        String key = "SLDS:LOAN:RATE:" + productKey;
        try {
            if (redisTemplate != null) {
                Object cached = redisTemplate.opsForValue().get(key);
                if (cached != null) {
                    log.info("Redis CACHE HIT for Interest Rate [{}]: {}", productKey, cached);
                    return new BigDecimal(cached.toString());
                }
                redisTemplate.opsForValue().set(key, defaultRate.toString(), Duration.ofHours(12));
            }
            return defaultRate;
        } catch (Exception e) {
            log.warn("Redis unavailable, using default rate: {}", e.getMessage());
            return defaultRate;
        }
    }
}
