package com.bank.slds.gateway.dto;

import java.time.LocalDateTime;

public record LogoutResponse(
    String username,
    String message,
    LocalDateTime loggedOutAt
) {}
