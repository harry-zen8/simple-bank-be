package com.example.banking.model;

import java.math.BigDecimal;

public class LimitedAccount extends Account {

    private static final BigDecimal BALANCE_LIMIT = new BigDecimal("10000");

    @Override
    public void setBalance(BigDecimal newBalance) {
        if (newBalance.compareTo(BALANCE_LIMIT) > 0) {
            throw new IllegalArgumentException("Balance for a limited account cannot exceed " + BALANCE_LIMIT);
        }
        super.setBalance(newBalance);
    }
} 