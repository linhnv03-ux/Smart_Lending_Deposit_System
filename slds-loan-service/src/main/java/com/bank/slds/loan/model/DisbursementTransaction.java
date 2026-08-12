package com.bank.slds.loan.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "disbursement_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisbursementTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String transactionId;

    @Column(nullable = false)
    private String contractNo;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String destinationAccount;

    @Column(nullable = false)
    private String coreBankingJournalRef;

    private String status;

    private String channel;

    private LocalDateTime createdAt;
}
