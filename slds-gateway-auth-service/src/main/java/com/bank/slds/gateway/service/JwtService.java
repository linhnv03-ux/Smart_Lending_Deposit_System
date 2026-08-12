package com.bank.slds.gateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

    private final ReactiveStringRedisTemplate redisTemplate;

    public JwtService(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String username, String role, String userId, String branchCode) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", userId);
        claims.put("branchCode", branchCode);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        String token = Jwts.builder()
            .setClaims(claims)
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();

        // Store active session in Redis with TTL
        String sessionKey = "SLDS:SESSION:" + username;
        redisTemplate.opsForValue().set(sessionKey, token, Duration.ofMillis(expirationMs)).subscribe();
        log.info("Issued JWT token for user {} (Role: {}) and saved in Redis session", username, role);

        return token;
    }

    public Mono<Boolean> validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

            String username = claims.getSubject();
            String sessionKey = "SLDS:SESSION:" + username;

            return redisTemplate.opsForValue().get(sessionKey)
                .map(cachedToken -> cachedToken.equals(token))
                .defaultIfEmpty(true); // Accept valid signed tokens even if Redis is unreachable
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return Mono.just(false);
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}
