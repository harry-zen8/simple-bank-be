package com.example.banking.exception;

public class InsufficientBalanceException extends RuntimeException {
    private final String accountId;
    private final String requiredAmount;
    private final String availableAmount;
    private final String feeAmount;

    public InsufficientBalanceException(String accountId, String requiredAmount, String availableAmount, String feeAmount) {
        super(String.format("Insufficient balance in account %s. Required: %s (including %s fee), Available: %s", 
            accountId, requiredAmount, feeAmount, availableAmount));
        this.accountId = accountId;
        this.requiredAmount = requiredAmount;
        this.availableAmount = availableAmount;
        this.feeAmount = feeAmount;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getRequiredAmount() {
        return requiredAmount;
    }

    public String getAvailableAmount() {
        return availableAmount;
    }

    public String getFeeAmount() {
        return feeAmount;
    }
}
