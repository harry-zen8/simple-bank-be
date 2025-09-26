package com.example.banking.service;

import com.example.banking.model.Account;
import java.math.BigDecimal;

public interface AccountOperations {

    /**
     * Performs a deposit into the given account.
     */
    void performDeposit(Account account, BigDecimal amount);

    /**
     * Performs a withdrawal from the given account.
     */
    void performWithdrawal(Account account, BigDecimal amount);

    /**
     * Applies interest to the account. Relevant only for savings-type accounts.
     */
    void applyInterest(Account account);

    /**
     * Processes an overdraft on the account. Relevant only for checking-type accounts.
     */
    void processOverdraft(Account account);
} 