package com.example.banking.model;

import lombok.Data;

@Data
public class AccountCreationRequest {
    private long customerId;
    private String accountType;
} 