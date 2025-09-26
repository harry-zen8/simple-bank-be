package com.example.banking.bdd;

import com.example.banking.model.Account;
import com.example.banking.service.AccountService;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccountStepDefinitions {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TestContext testContext;

    @Then("the balance of {string}'s account should be {double}")
    public void the_balance_of_s_account_should_be(String customerName, Double expectedBalance) {
        Account account = testContext.accounts.get(customerName);
        Account latestAccountState = accountService.getAccount(account.getId());
        assertEquals(0, BigDecimal.valueOf(expectedBalance).compareTo(latestAccountState.getBalance()));
    }

    @Then("the balance of {string}'s account should remain {double}")
    public void the_balance_of_s_account_should_remain(String customerName, Double expectedBalance) {
        the_balance_of_s_account_should_be(customerName, expectedBalance);
    }
} 