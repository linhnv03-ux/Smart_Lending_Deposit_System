package com.bank.slds.deposit.repository;

import com.bank.slds.deposit.model.DepositProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepositProductRepository extends JpaRepository<DepositProduct, Long> {
    Optional<DepositProduct> findByProductCode(String productCode);
    List<DepositProduct> findByActiveTrue();
}
