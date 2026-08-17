package com.bank.slds.gateway.dto;

import java.time.LocalDateTime;

public record RegisterResponse(
    String email,
    String username,
    String message,
    long expiresInSeconds,
    String otp, // Included for demonstration & development verification
    LocalDateTime requestedAt
) {}
