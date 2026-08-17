package com.bank.slds.gateway.dto;

import jakarta.validation.constraints.NotBlank;

public record ResendOtpRequest(
    @NotBlank(message = "Email is required")
    String email
) {}
