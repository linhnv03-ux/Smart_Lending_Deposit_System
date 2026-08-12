package com.bank.slds.deposit.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "deposit_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String productCode;

    @Column(nullable = false)
    private String productName;

    private Integer termMonths; // Term in months, 0 for demand deposit (không kỳ hạn)

    @Column(nullable = false)
    private BigDecimal interestRateAnnual; // % annual interest rate

    @Enumerated(EnumType.STRING)
    private InterestPayoutType payoutType;

    private BigDecimal minimumDepositAmount;

    private boolean active;
}
