package com.example.banking.controller;

import com.example.banking.model.Account;
import com.example.banking.model.AccountCreationRequest;
import com.example.banking.model.Customer;
import com.example.banking.model.CustomerCreationRequest;
import com.example.banking.model.Transaction;
import com.example.banking.model.ProcessTransactionRequest;
import com.example.banking.service.AccountService;
import com.example.banking.service.CustomerService;
import com.example.banking.service.FeeCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1") // Moving mapping to the class level and will define full paths below.
public class AccountController {

    @Autowired
    private AccountService accountService;
    @Autowired
    private FeeCalculationService feeCalculationService;
    @Autowired
    private CustomerService customerService;

    // --- Customer Endpoints (should be in CustomerController) ---

    @PostMapping("/customers")
    @Operation(summary = "Create a new customer")
    public Customer createCustomer(@RequestBody CustomerCreationRequest request) {
        return customerService.createCustomer(request);
    }

    @GetMapping("/customers/{id}")
    @Operation(summary = "Get customer by ID")
    public Customer getCustomer(@PathVariable long id) {
        return customerService.getCustomer(id);
    }

    @GetMapping("/customers")
    @Operation(summary = "Get all customers")
    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    // --- Account Endpoints ---

    @PostMapping("/accounts")
    @Operation(summary = "Create a new account for a customer")
    public Account createAccount(@RequestBody AccountCreationRequest request) {
        return accountService.createAccount(request);
    }

    @GetMapping("/accounts/{id}")
    @Operation(summary = "Get account by ID")
    public Account getAccount(@PathVariable long id) {
        return accountService.getAccount(id);
    }

    @GetMapping("/accounts")
    @Operation(summary = "Get accounts by customer ID", description = "Fetches all accounts for a given customer.")
    public List<Account> getAccountsByCustomer(@RequestParam("customerId") long customerId) {
        return accountService.getAccountsByCustomerId(customerId);
    }

    @GetMapping("/accounts/{id}/transactions")
    @Operation(summary = "Get all transactions for an account")
    public List<Transaction> getTransactions(@PathVariable long id) {
        return accountService.getTransactions(id);
    }

    @PostMapping("/accounts/process")
    @Operation(summary = "Process a transaction (DEPRECATED - Use more specific endpoints)", description = "Handles deposits, withdrawals, and transfers between accounts.")
    public ResponseEntity<Map<String, Object>> processTransaction(@RequestBody ProcessTransactionRequest request) {
        try {
            boolean result = accountService.processTransaction(request);
            
            Map<String, Object> response = new HashMap<>();
            if (result) {
                response.put("success", true);
                response.put("message", "Transaction processed successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("error", "TRANSACTION_FAILED");
                response.put("message", "Transaction failed. Please check the server logs for details.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "BALANCE_LIMIT_EXCEEDED");
            response.put("message", e.getMessage());
            response.put("lspViolation", true);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/accounts/{id}/fees")
    @Operation(summary = "Calculate and apply fees", description = "Triggers the fee calculation logic for a specific account.")
    public ResponseEntity<String> calculateFees(@Parameter(description = "ID of the account") @PathVariable long id) {
        try {
            String result = feeCalculationService.handle(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred during fee calculation: " + e.getMessage());
        }
    }
} 