package com.example.banking.bdd;

import com.example.banking.model.Account;
import com.example.banking.model.AccountCreationRequest;
import com.example.banking.model.Customer;
import com.example.banking.model.CustomerCreationRequest;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.CustomerRepository;
import com.example.banking.service.AccountService;
import com.example.banking.service.CustomerService;
import com.example.banking.service.FeeCalculationService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

public class FeeCalculationStepDefinitions {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private FeeCalculationService feeCalculationService;

    @Autowired
    private TestContext testContext;

    @Given("a customer {string} with a {string} status and an account having a balance of {double}")
    public void a_customer_with_a_status_and_an_account_having_a_balance_of(String customerName, String status, Double balance) {
        CustomerCreationRequest customerRequest = new CustomerCreationRequest();
        customerRequest.setName(customerName);
        Customer customer = customerService.createCustomer(customerRequest);
        customer.setCustomerLevel(status);
        customer = customerRepository.save(customer);
        testContext.customers.put(customerName, customer);

        AccountCreationRequest accountRequest = new AccountCreationRequest();
        accountRequest.setCustomerId(customer.getId());
        accountRequest.setAccountType("CHECKING");
        Account account = accountService.createAccount(accountRequest);
        account.setBalance(BigDecimal.valueOf(balance));
        accountRepository.save(account);
        testContext.accounts.put(customerName, account);
    }

    @Given("a customer {string} with a {string} status and a {word} account having a balance of {double}")
    public void a_customer_with_a_status_and_a_savings_account_having_a_balance_of(String customerName, String status, String accountType, Double balance) {
        CustomerCreationRequest customerRequest = new CustomerCreationRequest();
        customerRequest.setName(customerName);
        Customer customer = customerService.createCustomer(customerRequest);
        customer.setCustomerLevel(status);
        customer = customerRepository.save(customer);
        testContext.customers.put(customerName, customer);

        AccountCreationRequest accountRequest = new AccountCreationRequest();
        accountRequest.setCustomerId(customer.getId());
        accountRequest.setAccountType(accountType.toUpperCase());
        Account account = accountService.createAccount(accountRequest);
        account.setBalance(BigDecimal.valueOf(balance));
        accountRepository.save(account);
        testContext.accounts.put(customerName, account);
    }

    @When("the system calculates the monthly maintenance fees")
    public void the_system_calculates_the_monthly_maintenance_fees() {
        // In this BDD test, we iterate through all accounts created in the Given steps
        // and apply the fee calculation logic to each.
        for (Account account : testContext.accounts.values()) {
            feeCalculationService.handle(account.getId());
        }
    }
} 