package com.bank.slds.loan.repository;

import com.bank.slds.loan.model.DisbursementTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisbursementTransactionRepository extends JpaRepository<DisbursementTransaction, Long> {
    List<DisbursementTransaction> findByContractNo(String contractNo);
}
