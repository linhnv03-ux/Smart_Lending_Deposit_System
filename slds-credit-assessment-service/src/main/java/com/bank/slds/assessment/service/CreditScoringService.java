package com.bank.slds.assessment.service;

import com.bank.slds.assessment.dto.*;
import com.bank.slds.assessment.model.*;
import com.bank.slds.assessment.repository.CreditScoreRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditScoringService {

    private final CreditScoreRecordRepository recordRepository;
    private final ElasticsearchAssessmentLogService esLogService;

    @Transactional
    public CreditEvaluationResult evaluateCredit(CreditAssessmentMessage message) {
        String cip = message.applicantCip() != null ? message.applicantCip() : "001099887766";

        // 1. Calculate Score (300 - 850)
        int creditScore = 650 + (Math.abs(cip.hashCode()) % 190); // range 650 - 840

        // 2. CIC Lookup Simulation
        DebtGroup debtGroup = (creditScore >= 700) ? DebtGroup.GROUP_1_CURRENT : DebtGroup.GROUP_2_ATTENTION;
        boolean isBadDebt = creditScore < 600;

        String decision;
        String reason;
        BigDecimal approvedLimit = message.requestedAmount();

        if (isBadDebt) {
            decision = "REJECTED";
            reason = "Phát hiện nợ xấu CIC (Debt Group > 2). Tự động từ chối khoản vay.";
            approvedLimit = BigDecimal.ZERO;
        } else if (creditScore >= 720) {
            decision = "AUTO_APPROVED";
            reason = "Điểm tín dụng xuất sắc (" + creditScore + " điểm), không nợ xấu. Tự động phê duyệt 100% hạn mức.";
        } else if (creditScore >= 680) {
            decision = "APPROVED";
            reason = "Điểm tín dụng khá (" + creditScore + " điểm). Phê duyệt hồ sơ vay.";
        } else {
            decision = "MANUAL_REVIEW";
            reason = "Điểm tín dụng " + creditScore + " điểm. Chuyển cấp Thẩm định viên phê duyệt thủ công.";
            approvedLimit = message.requestedAmount().multiply(new BigDecimal("0.8"));
        }

        CreditScoreRecord record = CreditScoreRecord.builder()
            .applicationNo(message.applicationNo())
            .applicantId(message.applicantId())
            .applicantCip(cip)
            .creditScore(creditScore)
            .cicDebtGroup(debtGroup)
            .badDebtFound(isBadDebt)
            .maxRecommendLoanLimit(approvedLimit)
            .decisionStatus(decision)
            .decisionReason(reason)
            .evaluatedAt(LocalDateTime.now())
            .build();

        recordRepository.save(record);

        // Index in Elasticsearch
        String esLogId = esLogService.indexAssessmentLog(message.applicationNo(), cip, creditScore, decision, reason);

        return new CreditEvaluationResult(
            "EVAL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
            message.applicationNo(),
            cip,
            message.applicantName(),
            creditScore,
            debtGroup,
            isBadDebt,
            approvedLimit,
            decision,
            reason,
            LocalDateTime.now(),
            esLogId
        );
    }

    public CicHistoryDto queryCicHistory(String applicantCip) {
        int score = 650 + (Math.abs(applicantCip.hashCode()) % 190);
        DebtGroup debtGroup = (score >= 700) ? DebtGroup.GROUP_1_CURRENT : DebtGroup.GROUP_2_ATTENTION;

        return new CicHistoryDto(
            applicantCip,
            "Khách hàng " + applicantCip,
            debtGroup,
            1 + (Math.abs(applicantCip.hashCode()) % 3),
            new BigDecimal("150000000"),
            BigDecimal.ZERO,
            false,
            LocalDate.now().minusDays(2)
        );
    }
}
