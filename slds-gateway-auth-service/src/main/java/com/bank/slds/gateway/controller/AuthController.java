package com.bank.slds.gateway.controller;

import com.bank.slds.gateway.dto.AuthResponse;
import com.bank.slds.gateway.dto.LoginRequest;
import com.bank.slds.gateway.dto.TokenValidationResponse;
import com.bank.slds.gateway.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final JwtService jwtService;

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Authentication request received for user: {}", request.username());

        // Simulate Centralized Identity Verification / Active Directory Authentication
        String role = request.role() != null ? request.role() : "CREDIT_OFFICER";
        String userId = "USR-" + Math.abs(request.username().hashCode());
        String branchCode = "BRANCH_HO";

        String token = jwtService.generateToken(request.username(), role, userId, branchCode);

        AuthResponse response = new AuthResponse(
            true,
            token,
            "Bearer",
            86400000L,
            userId,
            request.username(),
            role,
            branchCode,
            LocalDateTime.now()
        );

        return Mono.just(ResponseEntity.ok(response));
    }

    @GetMapping("/validate")
    public Mono<ResponseEntity<TokenValidationResponse>> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just(ResponseEntity.ok(new TokenValidationResponse(false, null, null, null, "Invalid Authorization Header")));
        }

        String token = authHeader.substring(7);
        return jwtService.validateToken(token)
            .map(isValid -> {
                if (isValid) {
                    var claims = jwtService.getClaims(token);
                    return ResponseEntity.ok(new TokenValidationResponse(
                        true,
                        claims.getSubject(),
                        (String) claims.get("role"),
                        (String) claims.get("userId"),
                        "Token is valid and active in Redis session"
                    ));
                } else {
                    return ResponseEntity.ok(new TokenValidationResponse(false, null, null, null, "Token expired or invalidated"));
                }
            });
    }
}
