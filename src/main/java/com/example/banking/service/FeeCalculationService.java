package com.example.banking.service;

import com.example.banking.model.Account;
import com.example.banking.model.Customer;
import com.example.banking.model.Transaction;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.CustomerRepository;
import com.example.banking.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class FeeCalculationService {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Checks if a monthly fee was already applied to the account in the current month
     * @param accountId The account ID to check
     * @return true if fee was already applied this month, false otherwise
     */
    private boolean isMonthlyFeeAlreadyApplied(long accountId) {
        // Get current month start and end dates
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
        
        // Convert to Date objects for database query
        Date startDate = Date.from(monthStart.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(monthEnd.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
        
        // Check if there's already a FEE transaction for this account in the current month
        List<Transaction> feeTransactions = transactionRepository.findByAccountIdAndTypeAndDateBetween(
            accountId, "FEE", startDate, endDate);
        
        return !feeTransactions.isEmpty();
    }

    public String handle(long accountId) {
        // Check if monthly fee was already applied this month
        if (isMonthlyFeeAlreadyApplied(accountId)) {
            String message = "Monthly fee already charged for account " + accountId + " this month.";
            System.err.println(message);
            return message;
        }

        // High-level step: Get the data
        Optional<Account> accOpt = accountRepository.findById(accountId);
        if (accOpt.isEmpty()) {
            String message = "Account not found.";
            System.err.println(message);
            return message;
        }
        Account acc = accOpt.get();

        Optional<Customer> custOpt = customerRepository.findById(acc.getCustId());
        if (custOpt.isEmpty()) {
            String message = "Customer not found.";
            System.err.println(message);
            return message;
        }
        Customer cust = custOpt.get();

        // Intermediate-level step: Calculate fees
        FeeResult feeResult = new FeeResult();
        calculate(acc, cust.getCustomerLevel(), feeResult);

        // Low-level step: Apply the fees if they are not waived
        if (!feeResult.feesWaived) {
            acc.setBalance(acc.getBalance().subtract(feeResult.feeAmount));
            accountRepository.save(acc);

            Transaction t = new Transaction();
            t.setFromAccount(accountId);
            t.setValue(feeResult.feeAmount);
            t.setDate(new Date());
            t.setType("FEE");
            t.setDescription(feeResult.feeDescription);
            transactionRepository.save(t);

            String message = "Charged $" + feeResult.feeAmount + " fee to account " + accountId + " (" + feeResult.feeDescription + ")";
            System.out.println(message);
            return message;
        } else {
            String message = "No fee charged for account " + accountId + " (" + feeResult.feeDescription + ")";
            System.out.println(message);
            return message;
        }
    }

    private void calculate(Account account, String level, FeeResult res) {
        BigDecimal fee = new BigDecimal("10.00"); // Default maintenance fee
        res.feeDescription = "Monthly account fee";
        res.feesWaived = false;

        switch (level) {
            case "GOLD":
                res.feesWaived = true;
                res.feeAmount = BigDecimal.ZERO;
                res.feeDescription = "No fee for Gold customers";
                break;
            case "SILVER":
                // 50% discount for Silver members
                res.feeAmount = fee.multiply(new BigDecimal("0.5"));
                res.feeDescription = "Half price for Silver customers";
                break;
            case "BRONZE":
            default:
                // No discount for Bronze or default case
                res.feeAmount = fee;
                break;
        }

        if ("SAVINGS".equals(account.getAccountType()) && account.getBalance().compareTo(new BigDecimal("5000")) > 0) {
            res.feesWaived = true;
            res.feeDescription = "No fee for savings accounts with more than $5000";
        }
    }
} 