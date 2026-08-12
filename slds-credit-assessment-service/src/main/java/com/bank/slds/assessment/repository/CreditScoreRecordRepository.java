package com.bank.slds.assessment.repository;

import com.bank.slds.assessment.model.CreditScoreRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditScoreRecordRepository extends JpaRepository<CreditScoreRecord, Long> {
    Optional<CreditScoreRecord> findByApplicationNo(String applicationNo);
    List<CreditScoreRecord> findByApplicantCip(String applicantCip);
}
