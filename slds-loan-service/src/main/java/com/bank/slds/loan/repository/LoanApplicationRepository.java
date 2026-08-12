package com.bank.slds.loan.repository;

import com.bank.slds.loan.model.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    Optional<LoanApplication> findByApplicationNo(String applicationNo);
}
