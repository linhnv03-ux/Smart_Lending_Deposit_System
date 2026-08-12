package com.bank.slds.loan.service;

import com.bank.slds.loan.dto.CreditAssessmentMessage;
import com.bank.slds.loan.dto.LoanApplicationRequest;
import com.bank.slds.loan.dto.LoanApplicationResponse;
import com.bank.slds.loan.model.LoanApplication;
import com.bank.slds.loan.model.LoanStatus;
import com.bank.slds.loan.repository.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationService {

    private final LoanApplicationRepository applicationRepository;
    private final ActiveMqProducerService activeMqProducerService;
    private final ElasticsearchAuditService auditService;
    private final RedisCacheService redisCacheService;

    @Transactional
    public LoanApplicationResponse createApplication(LoanApplicationRequest request) {
        long startTime = System.currentTimeMillis();
        String appNo = "HS-VAY-" + (10000000 + new Random().nextInt(90000000));

        // Get or cache interest rate in Redis
        redisCacheService.getCachedInterestRate(request.interestType().name(), request.interestRate());

        LoanApplication entity = LoanApplication.builder()
            .applicationNo(appNo)
            .applicantId(request.applicantId())
            .applicantName(request.applicantName())
            .applicantCip(request.applicantCip())
            .requestedAmount(request.requestedAmount())
            .termMonths(request.termMonths())
            .interestRate(request.interestRate())
            .interestType(request.interestType())
            .purpose(request.purpose())
            .status(LoanStatus.SUBMITTED)
            .officerId(request.officerId() != null ? request.officerId() : "OFFICER_01")
            .branchCode(request.branchCode() != null ? request.branchCode() : "BRANCH_HO")
            .build();

        entity = applicationRepository.save(entity);

        // Publish Credit Assessment Event to ActiveMQ
        CreditAssessmentMessage amqMessage = new CreditAssessmentMessage(
            appNo,
            request.applicantId(),
            request.applicantName(),
            request.applicantCip(),
            request.requestedAmount(),
            request.termMonths(),
            LocalDateTime.now()
        );

        String activeMqMessageId = activeMqProducerService.publishCreditAssessmentEvent(amqMessage);

        long duration = System.currentTimeMillis() - startTime;
        auditService.recordAudit("CREATE_LOAN_APPLICATION", request.officerId(), appNo,
            "Created loan application for " + request.applicantName() + " amount " + request.requestedAmount(), "SUCCESS", duration);

        return new LoanApplicationResponse(
            entity.getId(),
            entity.getApplicationNo(),
            entity.getApplicantId(),
            entity.getApplicantName(),
            entity.getApplicantCip(),
            entity.getRequestedAmount(),
            entity.getTermMonths(),
            entity.getInterestRate(),
            entity.getInterestType(),
            entity.getPurpose(),
            entity.getStatus(),
            entity.getCreditScore(),
            entity.getOfficerId(),
            entity.getBranchCode(),
            activeMqMessageId,
            entity.getCreatedAt(),
            "Hồ sơ vay đã khởi tạo thành công và phát Event tới ActiveMQ để thẩm định tín dụng"
        );
    }

    public LoanApplicationResponse getApplication(String applicationNo) {
        LoanApplication app = applicationRepository.findByApplicationNo(applicationNo)
            .orElseThrow(() -> new IllegalArgumentException("Loan application not found: " + applicationNo));

        return new LoanApplicationResponse(
            app.getId(),
            app.getApplicationNo(),
            app.getApplicantId(),
            app.getApplicantName(),
            app.getApplicantCip(),
            app.getRequestedAmount(),
            app.getTermMonths(),
            app.getInterestRate(),
            app.getInterestType(),
            app.getPurpose(),
            app.getStatus(),
            app.getCreditScore(),
            app.getOfficerId(),
            app.getBranchCode(),
            "AMQ-PROCESSED",
            app.getCreatedAt(),
            "Thông tin hồ sơ vay"
        );
    }
}
