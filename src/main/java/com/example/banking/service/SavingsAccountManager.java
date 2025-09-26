package com.example.banking.service;

import com.example.banking.model.Account;
import java.math.BigDecimal;

public class SavingsAccountManager implements AccountOperations {

    private static final BigDecimal INTEREST_RATE = new BigDecimal("0.02");

    @Override
    public void performDeposit(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
        System.out.println("Deposited " + amount + " into Savings Account " + account.getId());
    }

    @Override
    public void performWithdrawal(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) >= 0) {
            account.setBalance(account.getBalance().subtract(amount));
            System.out.println("Withdrew " + amount + " from Savings Account " + account.getId());
        } else {
            System.out.println("Insufficient funds for withdrawal from Savings Account " + account.getId());
        }
    }

    @Override
    public void applyInterest(Account account) {
        BigDecimal interest = account.getBalance().multiply(INTEREST_RATE);
        account.setBalance(account.getBalance().add(interest));
        System.out.println("Applied interest of " + interest + " to Savings Account " + account.getId());
    }

    @Override
    public void processOverdraft(Account account) {
        throw new UnsupportedOperationException("Overdraft processing is not supported for savings accounts.");
    }
} 