package com.bank.slds.adapter.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "core_banking_journals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoreBankingJournal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String journalRef; // CB-JRN-XXXXX

    @Enumerated(EnumType.STRING)
    private JournalType journalType;

    private String debitAccount;  // Tài khoản ghi nợ

    private String creditAccount; // Tài khoản ghi có

    private BigDecimal amount;    // Số tiền hạch toán

    private String currency;      // VND

    private String status;        // SUCCESS, FAILED, QUEUED_OFFLINE

    private String responseMessage;

    private LocalDateTime postedAt;
}
