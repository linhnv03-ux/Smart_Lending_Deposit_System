package com.bank.slds.deposit.service;

import com.bank.slds.deposit.dto.*;
import com.bank.slds.deposit.model.*;
import com.bank.slds.deposit.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepositService {

    private final DepositProductRepository productRepository;
    private final DepositAccountRepository accountRepository;

    @Cacheable(value = "depositProducts")
    public List<DepositProductDto> getActiveProducts() {
        log.info("Fetching active deposit products from Database and Caching to Redis");
        List<DepositProduct> products = productRepository.findByActiveTrue();
        if (products.isEmpty()) {
            // Seed default catalog if empty
            products = List.of(
                DepositProduct.builder().productCode("TK-ONLINE-03M").productName("Tiết kiệm Online 3 Tháng").termMonths(3).interestRateAnnual(new BigDecimal("4.50")).payoutType(InterestPayoutType.AT_MATURITY).minimumDepositAmount(new BigDecimal("1000000")).active(true).build(),
                DepositProduct.builder().productCode("TK-ONLINE-06M").productName("Tiết kiệm Online 6 Tháng").termMonths(6).interestRateAnnual(new BigDecimal("5.20")).payoutType(InterestPayoutType.AT_MATURITY).minimumDepositAmount(new BigDecimal("1000000")).active(true).build(),
                DepositProduct.builder().productCode("TK-ONLINE-12M").productName("Tiết kiệm Online 12 Tháng").termMonths(12).interestRateAnnual(new BigDecimal("6.20")).payoutType(InterestPayoutType.AT_MATURITY).minimumDepositAmount(new BigDecimal("1000000")).active(true).build(),
                DepositProduct.builder().productCode("TK-TRA-LAI-HANG-THANG").productName("Tiết kiệm Lãi Hàng Tháng 12M").termMonths(12).interestRateAnnual(new BigDecimal("5.90")).payoutType(InterestPayoutType.MONTHLY).minimumDepositAmount(new BigDecimal("5000000")).active(true).build()
            );
            productRepository.saveAll(products);
        }

        return products.stream()
            .map(p -> new DepositProductDto(p.getProductCode(), p.getProductName(), p.getTermMonths(), p.getInterestRateAnnual(), p.getPayoutType(), p.getMinimumDepositAmount(), p.isActive()))
            .toList();
    }

    public CalculateInterestResponse calculateInterest(CalculateInterestRequest request) {
        BigDecimal principal = request.principal();
        BigDecimal annualRate = request.annualRate();
        int termMonths = request.termMonths();

        // Formula: Interest = Principal * (Rate / 100) * (termMonths / 12)
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
            .divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);

        BigDecimal fullTermInterest = principal.multiply(monthlyRate).multiply(BigDecimal.valueOf(termMonths))
            .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalPayout = principal.add(fullTermInterest);

        // Daily interest = Principal * Rate / 365
        BigDecimal dailyInterest = principal.multiply(annualRate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP))
            .divide(BigDecimal.valueOf(365), 2, RoundingMode.HALF_UP);

        // Demand deposit interest (0.2%/year) if early withdrawal
        BigDecimal demandRate = new BigDecimal("0.20");
        BigDecimal earlyInterest = principal.multiply(demandRate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP))
            .multiply(BigDecimal.valueOf(request.actualDaysHeld() > 0 ? request.actualDaysHeld() : 30))
            .divide(BigDecimal.valueOf(365), 2, RoundingMode.HALF_UP);

        return new CalculateInterestResponse(
            principal,
            annualRate,
            termMonths,
            fullTermInterest,
            totalPayout,
            dailyInterest,
            earlyInterest,
            "Interest = Principal * (Rate / 100) * (TermMonths / 12)"
        );
    }

    @Transactional
    public DepositAccountResponse openDepositAccount(OpenDepositRequest request) {
        String accNo = "STK-" + (1000000000L + new Random().nextInt(900000000));
        LocalDate openDate = LocalDate.now();
        int termMonths = request.termMonths() != null ? request.termMonths() : 12;
        LocalDate maturityDate = openDate.plusMonths(termMonths);

        BigDecimal rate = request.interestRate() != null ? request.interestRate() : new BigDecimal("6.20");
        InterestPayoutType payoutType = request.payoutType() != null ? request.payoutType() : InterestPayoutType.AT_MATURITY;

        CalculateInterestResponse interestCalc = calculateInterest(
            new CalculateInterestRequest(request.depositAmount(), rate, termMonths, 0, payoutType)
        );

        DepositAccount account = DepositAccount.builder()
            .accountNumber(accNo)
            .customerId(request.customerId())
            .customerName(request.customerName())
            .productCode(request.productCode())
            .principalAmount(request.depositAmount())
            .interestRateAnnual(rate)
            .termMonths(termMonths)
            .payoutType(payoutType)
            .openDate(openDate)
            .maturityDate(maturityDate)
            .status("ACTIVE")
            .accruedInterest(BigDecimal.ZERO)
            .createdAt(LocalDateTime.now())
            .build();

        accountRepository.save(account);

        log.info("Opened Deposit Account STK: {} for Customer: {}, Amount: {}", accNo, request.customerName(), request.depositAmount());

        return new DepositAccountResponse(
            true,
            accNo,
            request.customerId(),
            request.customerName(),
            request.productCode(),
            "Tiết kiệm có kỳ hạn " + termMonths + " Tháng",
            request.depositAmount(),
            rate,
            termMonths,
            payoutType,
            openDate,
            maturityDate,
            interestCalc.fullTermInterest(),
            interestCalc.totalPayoutAtMaturity(),
            "ACTIVE",
            "Mở sổ tiết kiệm thành công!"
        );
    }

    @Transactional
    public CloseDepositResponse closeDepositAccount(CloseDepositRequest request) {
        DepositAccount account = accountRepository.findByAccountNumber(request.accountNumber())
            .orElseThrow(() -> new IllegalArgumentException("Deposit account not found: " + request.accountNumber()));

        boolean isEarly = LocalDate.now().isBefore(account.getMaturityDate());
        BigDecimal appliedRate = isEarly ? new BigDecimal("0.20") : account.getInterestRateAnnual();

        CalculateInterestResponse calc = calculateInterest(
            new CalculateInterestRequest(
                account.getPrincipalAmount(),
                appliedRate,
                account.getTermMonths(),
                30,
                account.getPayoutType()
            )
        );

        BigDecimal interestEarned = isEarly ? calc.earlyWithdrawalInterest() : calc.fullTermInterest();
        BigDecimal totalPayout = account.getPrincipalAmount().add(interestEarned);

        account.setStatus("CLOSED");
        accountRepository.save(account);

        String receiptNo = "STK-CLOSE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        log.info("Closed Deposit Account STK: {}, EarlySettlement: {}, TotalPayout: {}", request.accountNumber(), isEarly, totalPayout);

        return new CloseDepositResponse(
            true,
            receiptNo,
            account.getAccountNumber(),
            account.getCustomerId(),
            account.getPrincipalAmount(),
            interestEarned,
            totalPayout,
            isEarly,
            appliedRate,
            LocalDateTime.now(),
            request.destinationAccount(),
            "CLOSED",
            isEarly ? "Tất toán trước hạn (Áp dụng lãi suất không kỳ hạn 0.2%/năm)" : "Tất toán đúng hạn thành công!"
        );
    }

    public List<DepositAccount> getAccountsByCustomer(String customerId) {
        return accountRepository.findByCustomerId(customerId);
    }
}
