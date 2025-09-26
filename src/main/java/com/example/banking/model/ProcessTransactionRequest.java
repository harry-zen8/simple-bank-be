package com.example.banking.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProcessTransactionRequest {
    private Long from;
    private Long to;
    private BigDecimal amount;
    private String type;
    private String currency;
    private String details;
    private boolean isPriority;
} 