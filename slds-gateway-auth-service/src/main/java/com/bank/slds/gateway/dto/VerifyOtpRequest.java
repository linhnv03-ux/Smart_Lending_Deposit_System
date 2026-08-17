package com.bank.slds.gateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyOtpRequest(
    @NotBlank(message = "Email is required")
    String email,

    @NotBlank(message = "OTP is required")
    @Size(min = 4, max = 8, message = "OTP must be between 4 and 8 digits")
    String otp
) {}
