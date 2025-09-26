package com.example.banking.exception;

public class AccountNotFoundException extends RuntimeException {
    private final String accountId;

    public AccountNotFoundException(String accountId) {
        super(String.format("Account with ID %s not found", accountId));
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }
}
