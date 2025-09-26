package com.example.banking.exception;

import java.math.BigDecimal;

public class TransactionLimitExceededException extends RuntimeException {
    private final String amount;
    private final String limit;
    private final String transactionType;

    public TransactionLimitExceededException(String amount, String limit, String transactionType) {
        super(String.format("Transaction amount %s exceeds the limit of %s for %s transactions. Manual approval required.", 
            amount, limit, transactionType));
        this.amount = amount;
        this.limit = limit;
        this.transactionType = transactionType;
    }

    public String getAmount() {
        return amount;
    }

    public String getLimit() {
        return limit;
    }

    public String getTransactionType() {
        return transactionType;
    }
}
