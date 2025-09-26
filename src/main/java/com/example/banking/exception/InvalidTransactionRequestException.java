package com.example.banking.exception;

public class InvalidTransactionRequestException extends RuntimeException {
    public InvalidTransactionRequestException(String message) {
        super(message);
    }
}
