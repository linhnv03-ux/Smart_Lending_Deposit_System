package com.bank.slds.deposit.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "deposit_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String accountNumber; // Số sổ tiết kiệm / TK Tiết kiệm

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String productCode;

    private BigDecimal principalAmount; // Số tiền gốc gửi

    private BigDecimal interestRateAnnual; // Lãi suất %/năm

    private Integer termMonths; // Kỳ hạn gửi (tháng)

    @Enumerated(EnumType.STRING)
    private InterestPayoutType payoutType;

    private LocalDate openDate;

    private LocalDate maturityDate; // Ngày đến hạn

    private String status; // ACTIVE, CLOSED, MATURED

    private BigDecimal accruedInterest; // Lãi dự thu / đã tích lũy

    private LocalDateTime createdAt;
}
