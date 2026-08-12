package com.bank.slds.loan.factory;

import com.bank.slds.loan.model.InterestType;
import com.bank.slds.loan.strategy.InterestCalculationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InterestStrategyFactory {

    private final Map<String, InterestCalculationStrategy> strategies;

    @Autowired
    public InterestStrategyFactory(Map<String, InterestCalculationStrategy> strategies) {
        this.strategies = strategies;
    }

    public InterestCalculationStrategy getStrategy(InterestType interestType) {
        String key = interestType != null ? interestType.name() : "REDUCING_BALANCE";
        InterestCalculationStrategy strategy = strategies.get(key);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported interest strategy type: " + interestType);
        }
        return strategy;
    }
}
