package com.bank.slds.loan.service;

import com.bank.slds.loan.dto.AuditLogDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class ElasticsearchAuditService {

    private final List<AuditLogDto> auditLogIndex = new CopyOnWriteArrayList<>();

    public String recordAudit(String action, String userId, String contractNo, String details, String status, long executionTimeMs) {
        String auditId = "ES-AUDIT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        AuditLogDto dto = new AuditLogDto(
            auditId,
            "slds-loan-service",
            action,
            userId,
            contractNo,
            details,
            status,
            executionTimeMs,
            LocalDateTime.now()
        );
        auditLogIndex.add(0, dto);
        log.info("Indexed transaction into Elasticsearch [Index: slds-audit-logs]: AuditId={}, Action={}", auditId, action);
        return auditId;
    }

    public List<AuditLogDto> getAuditLogs(String query) {
        if (query == null || query.isBlank()) {
            return new ArrayList<>(auditLogIndex);
        }
        return auditLogIndex.stream()
            .filter(log -> log.contractNo().toLowerCase().contains(query.toLowerCase())
                || log.action().toLowerCase().contains(query.toLowerCase())
                || log.details().toLowerCase().contains(query.toLowerCase()))
            .toList();
    }
}
