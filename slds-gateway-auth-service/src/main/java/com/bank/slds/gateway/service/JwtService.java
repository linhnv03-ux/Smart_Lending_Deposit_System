package com.bank.slds.gateway.service;

import com.bank.slds.gateway.security.CertificateKeyProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret:slds_super_secret_jwt_key_2026_bank_secure_key_123456}")
    private String secret;

    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

    @Value("${jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    private final ReactiveStringRedisTemplate redisTemplate;
    private final CertificateKeyProvider certificateKeyProvider;

    public JwtService(ReactiveStringRedisTemplate redisTemplate, CertificateKeyProvider certificateKeyProvider) {
        this.redisTemplate = redisTemplate;
        this.certificateKeyProvider = certificateKeyProvider;
    }

    private Key getSigningKey() {
        if (certificateKeyProvider != null && certificateKeyProvider.getPrivateKey() != null) {
            return certificateKeyProvider.getPrivateKey();
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private void configureVerification(JwtParserBuilder builder) {
        if (certificateKeyProvider != null && certificateKeyProvider.getPublicKey() != null) {
            builder.verifyWith(certificateKeyProvider.getPublicKey());
        } else {
            builder.verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public String generateToken(String username, String role, String userId, String branchCode) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", userId);
        claims.put("branchCode", branchCode);
        claims.put("type", "access");

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        String token = Jwts.builder()
            .claims(claims)
            .subject(username)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact();

        // Store active session in Redis
        String sessionKey = "SLDS:SESSION:" + username;
        redisTemplate.opsForValue().set(sessionKey, token, Duration.ofMillis(expirationMs)).subscribe();
        log.info("Issued JWT access token for user {} (Role: {}) via CertificateKeyProvider/HMAC and stored in Redis", username, role);

        return token;
    }

    public String generateRefreshToken(String username, String userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpirationMs);

        return Jwts.builder()
            .claims(claims)
            .subject(username)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact();
    }

    public Mono<Boolean> validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            String username = claims.getSubject();
            String sessionKey = "SLDS:SESSION:" + username;

            return redisTemplate.opsForValue().get(sessionKey)
                .map(cachedToken -> cachedToken.equals(token))
                .defaultIfEmpty(true);
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return Mono.just(false);
        }
    }

    public Claims getClaims(String token) {
        JwtParserBuilder builder = Jwts.parser();
        configureVerification(builder);
        return builder.build().parseSignedClaims(token).getPayload();
    }
}
