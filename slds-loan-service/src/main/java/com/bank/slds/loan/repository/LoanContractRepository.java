package com.bank.slds.loan.repository;

import com.bank.slds.loan.model.LoanContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoanContractRepository extends JpaRepository<LoanContract, Long> {
    Optional<LoanContract> findByContractNo(String contractNo);
    Optional<LoanContract> findByApplicationNo(String applicationNo);
}
