package com.bank.slds.adapter.repository;

import com.bank.slds.adapter.model.CoreBankingJournal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoreBankingJournalRepository extends JpaRepository<CoreBankingJournal, Long> {
    Optional<CoreBankingJournal> findByJournalRef(String journalRef);
}
