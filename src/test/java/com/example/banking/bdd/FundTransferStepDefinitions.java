package com.example.banking.bdd;

import com.example.banking.model.Account;
import com.example.banking.model.AccountCreationRequest;
import com.example.banking.model.Customer;
import com.example.banking.model.CustomerCreationRequest;
import com.example.banking.model.ProcessTransactionRequest;
import com.example.banking.repository.AccountRepository;
import com.example.banking.service.AccountService;
import com.example.banking.service.CustomerService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class FundTransferStepDefinitions {

    @Autowired
    private CustomerService customerService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TestContext testContext;

    @Given("a customer {string} with an account having a balance of {double}")
    public void a_customer_with_an_account_having_a_balance_of(String customerName, Double balance) {
        // Check if customer already exists in test context
        Customer customer = testContext.customers.get(customerName);
        if (customer == null) {
            CustomerCreationRequest customerRequest = new CustomerCreationRequest();
            customerRequest.setName(customerName);
            customerRequest.setEmail(customerName.toLowerCase() + "@example.com");
            customerRequest.setPhone("1234567890");
            try {
                customer = customerService.createCustomer(customerRequest);
            } catch (IllegalArgumentException e) {
                // Customer already exists, find it by name
                customer = customerService.findByName(customerName).orElseThrow(
                    () -> new RuntimeException("Customer " + customerName + " not found"));
            }
            testContext.customers.put(customerName, customer);
        }

        AccountCreationRequest accountRequest = new AccountCreationRequest();
        accountRequest.setCustomerId(customer.getId());
        accountRequest.setAccountType("CHECKING");
        Account account = accountService.createAccount(accountRequest);
        account.setBalance(BigDecimal.valueOf(balance));
        accountRepository.save(account);
        testContext.accounts.put(customerName, account);
    }

    @When("{string} transfers {double} to {string}")
    public void transfers_to(String fromCustomerName, Double amount, String toCustomerName) {
        Account fromAccount = testContext.accounts.get(fromCustomerName);
        Account toAccount = testContext.accounts.get(toCustomerName);

        ProcessTransactionRequest transactionRequest = new ProcessTransactionRequest();
        transactionRequest.setFrom(fromAccount.getId());
        transactionRequest.setTo(toAccount.getId());
        transactionRequest.setAmount(BigDecimal.valueOf(amount));
        transactionRequest.setType("TRANSFER");
        
        testContext.lastTransactionStatus = accountService.processTransaction(transactionRequest);
    }

    @Then("the transfer should fail")
    public void the_transfer_should_fail() {
        assertFalse(testContext.lastTransactionStatus);
    }
} 