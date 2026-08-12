package com.bank.slds.loan.repository;

import com.bank.slds.loan.model.RepaymentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepaymentScheduleRepository extends JpaRepository<RepaymentSchedule, Long> {
    List<RepaymentSchedule> findByContractNoOrderByPeriodAsc(String contractNo);
}
