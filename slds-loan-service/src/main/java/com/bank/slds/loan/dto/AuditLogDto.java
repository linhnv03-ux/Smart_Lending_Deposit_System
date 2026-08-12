package com.bank.slds.loan.dto;

import java.time.LocalDateTime;

public record AuditLogDto(
    String auditId,
    String service,
    String action,
    String userId,
    String contractNo,
    String details,
    String status,
    long executionTimeMs,
    LocalDateTime timestamp
) {}
