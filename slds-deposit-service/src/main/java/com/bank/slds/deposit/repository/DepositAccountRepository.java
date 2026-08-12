package com.bank.slds.deposit.repository;

import com.bank.slds.deposit.model.DepositAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepositAccountRepository extends JpaRepository<DepositAccount, Long> {
    Optional<DepositAccount> findByAccountNumber(String accountNumber);
    List<DepositAccount> findByCustomerId(String customerId);
}
