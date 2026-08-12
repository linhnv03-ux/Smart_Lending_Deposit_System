package com.bank.slds.gateway.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Username is required")
    String username,

    @NotBlank(message = "Password is required")
    String password,

    String role // TELLER, CREDIT_OFFICER, BRANCH_MANAGER, CLIENT
) {}
