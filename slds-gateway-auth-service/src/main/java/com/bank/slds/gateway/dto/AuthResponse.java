package com.bank.slds.gateway.dto;

import java.time.LocalDateTime;

public record AuthResponse(
    String token,
    String refreshToken,
    String tokenType,
    long expiresInMs,
    String userId,
    String username,
    String role,
    String branchCode,
    LocalDateTime issuedAt
) {}
