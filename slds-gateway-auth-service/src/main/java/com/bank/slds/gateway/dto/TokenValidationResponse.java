package com.bank.slds.gateway.dto;

public record TokenValidationResponse(
    boolean valid,
    String username,
    String role,
    String userId,
    String message
) {}
