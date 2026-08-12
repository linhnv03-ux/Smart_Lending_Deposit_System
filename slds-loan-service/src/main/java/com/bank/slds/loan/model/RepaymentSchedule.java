package com.bank.slds.loan.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "repayment_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepaymentSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String contractNo;

    @Column(nullable = false)
    private Integer period;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    private BigDecimal principalPayable;

    @Column(nullable = false)
    private BigDecimal interestPayable;

    @Column(nullable = false)
    private BigDecimal totalInstallment;

    @Column(nullable = false)
    private BigDecimal remainingBalance;

    private boolean isPaid;

    private LocalDate paidDate;
}
