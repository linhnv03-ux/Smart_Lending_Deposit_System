package com.bank.slds.assessment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class ElasticsearchAssessmentLogService {

    private final List<Map<String, Object>> esLogIndex = new ArrayList<>();

    public String indexAssessmentLog(String applicationNo, String applicantCip, int score, String decision, String details) {
        String logId = "ES-CREDIT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("logId", logId);
        logEntry.put("index", "slds-credit-assessment-logs");
        logEntry.put("applicationNo", applicationNo);
        logEntry.put("applicantCip", applicantCip);
        logEntry.put("creditScore", score);
        logEntry.put("decision", decision);
        logEntry.put("details", details);
        logEntry.put("timestamp", LocalDateTime.now().toString());

        esLogIndex.add(0, logEntry);
        log.info("Elasticsearch INDEXED Credit Log [Index: slds-credit-assessment-logs]: LogId={}, AppNo={}, Score={}, Decision={}",
            logId, applicationNo, score, decision);
        return logId;
    }

    public List<Map<String, Object>> searchLogs(String query) {
        if (query == null || query.isBlank()) {
            return new ArrayList<>(esLogIndex);
        }
        return esLogIndex.stream()
            .filter(entry -> entry.get("applicationNo").toString().toLowerCase().contains(query.toLowerCase())
                || entry.get("applicantCip").toString().toLowerCase().contains(query.toLowerCase())
                || entry.get("decision").toString().toLowerCase().contains(query.toLowerCase()))
            .toList();
    }
}
