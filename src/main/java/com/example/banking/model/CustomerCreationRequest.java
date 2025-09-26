package com.example.banking.model;

import lombok.Data;

// This DTO is used to represent the payload for creating a new customer.
@Data
public class CustomerCreationRequest {
    private String name;
    private String email;
    private String phone;
} 