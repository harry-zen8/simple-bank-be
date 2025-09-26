package com.example.banking.bdd;

import com.example.banking.model.Account;
import com.example.banking.model.Customer;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope("cucumber-glue")
public class TestContext {
    public final Map<String, Customer> customers = new HashMap<>();
    public final Map<String, Account> accounts = new HashMap<>();
    public boolean lastTransactionStatus;
} 