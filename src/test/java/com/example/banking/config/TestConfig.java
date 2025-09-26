package com.example.banking.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;

/**
 * Test configuration for TDD exercises and unit tests.
 * This configuration provides test-specific beans and settings.
 */
@TestConfiguration
@Profile("test")
public class TestConfig {

    /**
     * Test constants for consistent testing across the application
     */
    public static class TestConstants {
        public static final BigDecimal STUDENT_ACCOUNT_LIMIT = new BigDecimal("10000");
        public static final BigDecimal INTERNATIONAL_TRANSFER_FEE = new BigDecimal("50.00");
        public static final BigDecimal LARGE_TRANSACTION_THRESHOLD = new BigDecimal("10000");
        public static final BigDecimal BASE_MAINTENANCE_FEE = new BigDecimal("10.00");
        public static final BigDecimal SAVINGS_EXEMPTION_LIMIT = new BigDecimal("5000");
        public static final BigDecimal SAVINGS_INTEREST_RATE = new BigDecimal("0.02");
        public static final BigDecimal SILVER_DISCOUNT_RATE = new BigDecimal("0.5");
        
        // Test customer IDs
        public static final Long TEST_CUSTOMER_ID = 1L;
        public static final Long TEST_CUSTOMER_ID_2 = 2L;
        
        // Test account IDs
        public static final Long TEST_ACCOUNT_ID = 1L;
        public static final Long TEST_ACCOUNT_ID_2 = 2L;
        
        // Test amounts
        public static final BigDecimal TEST_AMOUNT_SMALL = new BigDecimal("100");
        public static final BigDecimal TEST_AMOUNT_MEDIUM = new BigDecimal("1000");
        public static final BigDecimal TEST_AMOUNT_LARGE = new BigDecimal("15000");
    }

    /**
     * Test data factory for creating consistent test objects
     */
    @Bean
    @Primary
    public TestDataFactory testDataFactory() {
        return new TestDataFactory();
    }

    /**
     * Factory class for creating test data objects
     */
    public static class TestDataFactory {
        
        public com.example.banking.model.Customer createTestCustomer(Long id, String name, String level) {
            com.example.banking.model.Customer customer = new com.example.banking.model.Customer();
            customer.setId(id);
            customer.setName(name);
            customer.setCustomerLevel(level);
            return customer;
        }
        
        public com.example.banking.model.Customer createBronzeCustomer(Long id) {
            return createTestCustomer(id, "Test Customer " + id, "BRONZE");
        }
        
        public com.example.banking.model.Customer createSilverCustomer(Long id) {
            return createTestCustomer(id, "Test Customer " + id, "SILVER");
        }
        
        public com.example.banking.model.Customer createGoldCustomer(Long id) {
            return createTestCustomer(id, "Test Customer " + id, "GOLD");
        }
        
        public com.example.banking.model.Account createTestAccount(Long id, Long customerId, String type, BigDecimal balance) {
            com.example.banking.model.Account account = new com.example.banking.model.Account();
            account.setId(id);
            account.setCustId(customerId);
            account.setAccountType(type);
            account.setBalance(balance);
            return account;
        }
        
        public com.example.banking.model.Account createStudentAccount(Long id, Long customerId, BigDecimal balance) {
            return createTestAccount(id, customerId, "STUDENT", balance);
        }
        
        public com.example.banking.model.Account createSavingsAccount(Long id, Long customerId, BigDecimal balance) {
            return createTestAccount(id, customerId, "SAVINGS", balance);
        }
        
        public com.example.banking.model.Account createCheckingAccount(Long id, Long customerId, BigDecimal balance) {
            return createTestAccount(id, customerId, "CHECKING", balance);
        }
        
        public com.example.banking.model.Transaction createTestTransaction(Long id, Long fromAccount, Long toAccount, 
                                                                          BigDecimal amount, String type) {
            com.example.banking.model.Transaction transaction = new com.example.banking.model.Transaction();
            transaction.setId(id);
            transaction.setFromAccount(fromAccount);
            transaction.setToAccount(toAccount);
            transaction.setValue(amount);
            transaction.setType(type);
            transaction.setDate(new java.util.Date());
            return transaction;
        }
        
        public com.example.banking.model.ProcessTransactionRequest createTransferRequest(Long from, Long to, BigDecimal amount) {
            com.example.banking.model.ProcessTransactionRequest request = new com.example.banking.model.ProcessTransactionRequest();
            request.setFrom(from);
            request.setTo(to);
            request.setAmount(amount);
            request.setType("TRANSFER");
            return request;
        }
        
        public com.example.banking.model.ProcessTransactionRequest createInternationalTransferRequest(Long from, Long to, BigDecimal amount) {
            com.example.banking.model.ProcessTransactionRequest request = new com.example.banking.model.ProcessTransactionRequest();
            request.setFrom(from);
            request.setTo(to);
            request.setAmount(amount);
            request.setType("INTERNATIONAL_TRANSFER");
            return request;
        }
        
        public com.example.banking.model.ProcessTransactionRequest createDepositRequest(Long to, BigDecimal amount) {
            com.example.banking.model.ProcessTransactionRequest request = new com.example.banking.model.ProcessTransactionRequest();
            request.setTo(to);
            request.setAmount(amount);
            request.setType("DEPOSIT");
            return request;
        }
        
        public com.example.banking.model.ProcessTransactionRequest createWithdrawalRequest(Long from, BigDecimal amount) {
            com.example.banking.model.ProcessTransactionRequest request = new com.example.banking.model.ProcessTransactionRequest();
            request.setFrom(from);
            request.setAmount(amount);
            request.setType("WITHDRAWAL");
            return request;
        }
    }
}
