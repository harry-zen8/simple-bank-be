package com.example.banking.service;

import com.example.banking.model.Account;
import com.example.banking.model.AccountCreationRequest;
import com.example.banking.model.LimitedAccount;
import com.example.banking.model.ProcessTransactionRequest;
import com.example.banking.model.Transaction;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    private final EmailNotificationService notificationService = new EmailNotificationService();

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public Account createAccount(AccountCreationRequest request) {
        if ("STUDENT".equals(request.getAccountType())) {
            return createLimitedAccount(request);
        }
        
        Account account = new Account();
        account.setCustId(request.getCustomerId());
        account.setAccountType(request.getAccountType());
        account.setBalance(BigDecimal.ZERO);
        return accountRepository.save(account);
    }

    public Account createLimitedAccount(AccountCreationRequest request) {
        Account account = new Account();
        account.setCustId(request.getCustomerId());
        account.setAccountType("STUDENT");
        account.setBalance(BigDecimal.ZERO);
        return accountRepository.save(account);
    }

    public Account getAccount(long id) {
        return accountRepository.findById(id).orElse(null);
    }

    public List<Account> getAccountsByCustomerId(long customerId) {
        return accountRepository.findAccountsByCustomerId(customerId);
    }

    public List<Transaction> getTransactions(long accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    public boolean processTransaction(ProcessTransactionRequest request) {
        Long fromId = request.getFrom();
        Long toId = request.getTo();
        BigDecimal amount = request.getAmount();
        String type = request.getType();

        // Validate request
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.err.println("Invalid transaction: amount must be more than zero");
            return false;
        }
        
        if (type == null || type.trim().isEmpty()) {
            System.err.println("Invalid transaction: transaction type is required");
            return false;
        }

        // Log everything for now, we'll figure out logging later
        System.out.println("Processing transaction: from=" + fromId + ", to=" + toId + ", amount=" + amount + ", type=" + type);

        // Check transaction limit for large amounts
        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            // Send notification for large transaction but don't block it
            System.out.println("Transaction amount " + amount + " exceeds the limit of 10000.");
            if (fromId != null) {
                accountRepository.findById(fromId).ifPresent(acc ->
                        notificationService.sendNotification(String.valueOf(acc.getCustId()), "A large transaction of " + amount + " was initiated."));
            }
        }

        if ("TRANSFER".equalsIgnoreCase(type) || "INTERNATIONAL_TRANSFER".equalsIgnoreCase(type)) {
            if (fromId == null || toId == null) {
                System.err.println("Invalid transfer: need both from and to account numbers");
                return false;
            }

            Optional<Account> fromAccOpt = accountRepository.findById(fromId);
            Optional<Account> toAccOpt = accountRepository.findById(toId);

            if (fromAccOpt.isEmpty()) {
                System.err.println("Account " + fromId + " not found");
                return false;
            }
            
            if (toAccOpt.isEmpty()) {
                System.err.println("Account " + toId + " not found");
                return false;
            }

            Account fromAcc = fromAccOpt.get();
            Account toAcc = toAccOpt.get();

            BigDecimal totalDebit = amount;
            BigDecimal feeAmount = BigDecimal.ZERO;
            
            if ("INTERNATIONAL_TRANSFER".equalsIgnoreCase(type)) {
                feeAmount = new BigDecimal("50.00"); // Magic number for fee
                totalDebit = totalDebit.add(feeAmount);
                System.out.println("Charged international fee of: " + feeAmount);
            }

            if (fromAcc.getBalance().compareTo(totalDebit) < 0) {
                System.err.println("Not enough money in account " + fromId + 
                    ". Need: $" + totalDebit + " (including $" + feeAmount + " fee), Have: $" + fromAcc.getBalance());
                return false;
            }

            // Process the transaction
            fromAcc.setBalance(fromAcc.getBalance().subtract(totalDebit));
            toAcc.setBalance(toAcc.getBalance().add(amount));

            accountRepository.save(fromAcc);
            accountRepository.save(toAcc);

            // Save the main transfer transaction
            Transaction t = new Transaction();
            t.setFromAccount(fromId);
            t.setToAccount(toId);
            t.setValue(amount);
            t.setDate(new Date());
            t.setType(type.toUpperCase());
            t.setDescription(request.getDetails());
            transactionRepository.save(t);
            
            // Save the international transfer fee as a separate transaction
            if ("INTERNATIONAL_TRANSFER".equalsIgnoreCase(type)) {
                Transaction feeTransaction = new Transaction();
                feeTransaction.setFromAccount(fromId);
                feeTransaction.setToAccount(null); // Fee goes to the bank, not to another account
                feeTransaction.setValue(feeAmount);
                feeTransaction.setDate(new Date());
                feeTransaction.setType("FEE");
                feeTransaction.setDescription("International transfer fee");
                transactionRepository.save(feeTransaction);
                
                notificationService.sendNotification(String.valueOf(fromAcc.getCustId()), "International transfer processed.");
            }
        } else if ("DEPOSIT".equalsIgnoreCase(type)) {
            if (toId == null) {
                System.err.println("Need account number for deposit");
                return false;
            }
            
            Optional<Account> accOpt = accountRepository.findById(toId);
            if (accOpt.isEmpty()) {
                System.err.println("Account " + toId + " not found");
                return false;
            }

            Account acc = accOpt.get();
            
            if ("STUDENT".equals(acc.getAccountType())) {
                BigDecimal newBalance = acc.getBalance().add(amount);
                if (newBalance.compareTo(new BigDecimal("10000")) > 0) {
                    throw new IllegalArgumentException("Balance for a limited account cannot exceed 10000");
                }
            }
            
            acc.setBalance(acc.getBalance().add(amount));
            accountRepository.save(acc);

            Transaction t = new Transaction();
            t.setToAccount(toId);
            t.setValue(amount);
            t.setDate(new Date());
            t.setType("DEPOSIT");
            t.setDescription(request.getDetails());
            transactionRepository.save(t);

        } else if ("WITHDRAWAL".equalsIgnoreCase(type)) {
            if (fromId == null) {
                System.err.println("Need account number for withdrawal");
                return false;
            }
            
            Optional<Account> accOpt = accountRepository.findById(fromId);
            if (accOpt.isEmpty()) {
                System.err.println("Account " + fromId + " not found");
                return false;
            }

            Account acc = accOpt.get();
            if (acc.getBalance().compareTo(amount) < 0) {
                System.err.println("Not enough money in account " + fromId + 
                    ". Need: $" + amount + ", Have: $" + acc.getBalance());
                return false;
            }

            acc.setBalance(acc.getBalance().subtract(amount));
            accountRepository.save(acc);

            Transaction t = new Transaction();
            t.setFromAccount(fromId);
            t.setValue(amount);
            t.setDate(new Date());
            t.setType("WITHDRAWAL");
            t.setDescription(request.getDetails());
            transactionRepository.save(t);
        } else {
            System.err.println("Invalid transaction type: " + type);
            return false;
        }

        return true; // Success
    }
} 