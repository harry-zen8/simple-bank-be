package com.example.banking.bdd;

import com.example.banking.model.*;
import com.example.banking.repository.AccountRepository;
import com.example.banking.service.AccountService;
import com.example.banking.service.CustomerService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class AccountManagementStepDefinitions {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TestContext testContext;

    private boolean lastTransactionStatus;

    @Given("a customer {string} with a {string} account having a balance of {double}")
    public void a_customer_with_a_account_having_a_balance_of(String customerName, String accountType, Double balance) {
        CustomerCreationRequest customerRequest = new CustomerCreationRequest();
        customerRequest.setName(customerName);
        Customer customer = customerService.createCustomer(customerRequest);
        testContext.customers.put(customerName, customer);

        AccountCreationRequest accountRequest = new AccountCreationRequest();
        accountRequest.setCustomerId(customer.getId());
        accountRequest.setAccountType(accountType);
        Account account = accountService.createAccount(accountRequest);
        account.setBalance(BigDecimal.valueOf(balance));
        accountRepository.save(account);
        testContext.accounts.put(customerName, account);
    }

    @When("a new {string} account is created for {string}")
    public void a_new_account_is_created_for(String accountType, String customerName) {
        Customer customer = testContext.customers.get(customerName);
        if (customer == null) {
            // If the customer was created in a different step definition, we need to fetch them
            customer = customerService.findByName(customerName).orElseThrow();
            testContext.customers.put(customerName, customer);
        }

        AccountCreationRequest accountRequest = new AccountCreationRequest();
        accountRequest.setCustomerId(customer.getId());
        accountRequest.setAccountType(accountType);
        Account account = accountService.createAccount(accountRequest);
        testContext.accounts.put(customerName, account);
    }

    @Then("{string} should have a {string} account")
    public void should_have_an_account(String customerName, String accountType) {
        Customer customer = testContext.customers.get(customerName);
        Optional<Account> accountOpt = accountRepository.findAccountsByCustomerId(customer.getId())
                .stream()
                .filter(a -> a.getAccountType().equals(accountType))
                .findFirst();
        assertTrue(accountOpt.isPresent(), "Account should have been created for the customer");
        assertEquals(accountType, accountOpt.get().getAccountType());
    }

    @When("{string} deposits {double} into their {string} account")
    public void deposits_into_their_account(String customerName, Double amount, String accountType) {
        Account account = testContext.accounts.get(customerName);
        ProcessTransactionRequest transactionRequest = new ProcessTransactionRequest();
        transactionRequest.setTo(account.getId());
        transactionRequest.setAmount(BigDecimal.valueOf(amount));
        transactionRequest.setType("DEPOSIT");
        lastTransactionStatus = accountService.processTransaction(transactionRequest);
    }

    @When("{string} withdraws {double} from their {string} account")
    public void withdraws_from_their_account(String customerName, Double amount, String accountType) {
        Account account = testContext.accounts.get(customerName);
        ProcessTransactionRequest transactionRequest = new ProcessTransactionRequest();
        transactionRequest.setFrom(account.getId());
        transactionRequest.setAmount(BigDecimal.valueOf(amount));
        transactionRequest.setType("WITHDRAWAL");
        lastTransactionStatus = accountService.processTransaction(transactionRequest);
    }

    @Then("the balance of {string}'s {string} account should be {double}")
    public void the_balance_of_s_account_should_be(String customerName, String accountType, Double expectedBalance) {
        Account account = testContext.accounts.get(customerName);
        Account latestAccountState = accountService.getAccount(account.getId());
        assertEquals(0, BigDecimal.valueOf(expectedBalance).compareTo(latestAccountState.getBalance()));
    }

    @Then("the withdrawal should fail")
    public void the_withdrawal_should_fail() {
        assertFalse(lastTransactionStatus);
    }

    @Then("the balance of {string}'s {string} account should remain {double}")
    public void the_balance_of_s_account_should_remain(String customerName, String accountType, Double expectedBalance) {
        the_balance_of_s_account_should_be(customerName, accountType, expectedBalance);
    }
} 