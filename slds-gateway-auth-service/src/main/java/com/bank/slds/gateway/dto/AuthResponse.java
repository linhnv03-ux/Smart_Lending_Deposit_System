package com.bank.slds.gateway.dto;

import java.time.LocalDateTime;

public record AuthResponse(
    boolean success,
    String token,
    String tokenType,
    long expiresInMs,
    String userId,
    String username,
    String role,
    String branchCode,
    LocalDateTime issuedAt
) {}
