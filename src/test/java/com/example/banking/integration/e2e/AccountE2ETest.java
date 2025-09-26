package com.example.banking.integration.e2e;

import com.example.banking.model.Account;
import com.example.banking.model.AccountCreationRequest;
import com.example.banking.model.Customer;
import com.example.banking.model.CustomerCreationRequest;
import com.example.banking.model.ProcessTransactionRequest;
import com.example.banking.model.Transaction;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.CustomerRepository;
import com.example.banking.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class AccountE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1";
        // Clean up test data
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void shouldCreateCustomerAndAccountCompleteFlow() {
        // Given
        CustomerCreationRequest customerRequest = new CustomerCreationRequest();
        customerRequest.setName("E2E Test Customer");
        customerRequest.setEmail("e2e@example.com");
        customerRequest.setPhone("1234567890");

        // When - Create customer
        ResponseEntity<Customer> customerResponse = restTemplate.postForEntity(
                baseUrl + "/customers", customerRequest, Customer.class);

        // Then - Verify customer creation
        assertThat(customerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Customer createdCustomer = customerResponse.getBody();
        assertThat(createdCustomer).isNotNull();
        assertThat(createdCustomer.getId()).isNotNull();
        assertThat(createdCustomer.getName()).isEqualTo("E2E Test Customer");
        assertThat(createdCustomer.getEmail()).isEqualTo("e2e@example.com");

        // Given - Create account for the customer
        AccountCreationRequest accountRequest = new AccountCreationRequest();
        accountRequest.setCustomerId(createdCustomer.getId());
        accountRequest.setAccountType("CHECKING");

        // When - Create account
        ResponseEntity<Account> accountResponse = restTemplate.postForEntity(
                baseUrl + "/accounts", accountRequest, Account.class);

        // Then - Verify account creation
        assertThat(accountResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Account createdAccount = accountResponse.getBody();
        assertThat(createdAccount).isNotNull();
        assertThat(createdAccount.getId()).isNotNull();
        assertThat(createdAccount.getCustId()).isEqualTo(createdCustomer.getId());
        assertThat(createdAccount.getAccountType()).isEqualTo("CHECKING");
        assertThat(createdAccount.getBalance()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void shouldProcessDepositTransactionE2E() {
        // Given - Create customer and account
        Customer customer = createTestCustomer();
        Account account = createTestAccount(customer.getId());

        ProcessTransactionRequest depositRequest = new ProcessTransactionRequest();
        depositRequest.setTo(account.getId());
        depositRequest.setAmount(BigDecimal.valueOf(500.00));
        depositRequest.setType("DEPOSIT");

        // When - Process deposit
        ResponseEntity<Map> transactionResponse = restTemplate.postForEntity(
                baseUrl + "/accounts/process", depositRequest, Map.class);

        // Then - Verify transaction success
        assertThat(transactionResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = transactionResponse.getBody();
        assertThat(responseBody.get("success")).isEqualTo(true);
        assertThat(responseBody.get("message")).isEqualTo("Transaction processed successfully");

        // Verify account balance updated
        ResponseEntity<Account> updatedAccountResponse = restTemplate.getForEntity(
                baseUrl + "/accounts/" + account.getId(), Account.class);
        Account updatedAccount = updatedAccountResponse.getBody();
        assertThat(updatedAccount.getBalance().compareTo(BigDecimal.valueOf(500.00))).isEqualTo(0);

        // Verify transaction recorded
        ResponseEntity<Transaction[]> transactionsResponse = restTemplate.getForEntity(
                baseUrl + "/accounts/" + account.getId() + "/transactions", Transaction[].class);
        Transaction[] transactions = transactionsResponse.getBody();
        assertThat(transactions).hasSize(1);
        assertThat(transactions[0].getType()).isEqualTo("DEPOSIT");
        assertThat(transactions[0].getValue().compareTo(BigDecimal.valueOf(500.00))).isEqualTo(0);
    }

    @Test
    void shouldProcessWithdrawalTransactionE2E() {
        // Given - Create customer and account, then deposit money first
        Customer customer = createTestCustomer();
        Account account = createTestAccount(customer.getId());
        
        // First deposit money to set initial balance
        ProcessTransactionRequest depositRequest = new ProcessTransactionRequest();
        depositRequest.setTo(account.getId());
        depositRequest.setAmount(BigDecimal.valueOf(1000.00));
        depositRequest.setType("DEPOSIT");
        
        ResponseEntity<Map> depositResponse = restTemplate.postForEntity(
                baseUrl + "/accounts/process", depositRequest, Map.class);
        assertThat(depositResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Now test withdrawal
        ProcessTransactionRequest withdrawalRequest = new ProcessTransactionRequest();
        withdrawalRequest.setFrom(account.getId());
        withdrawalRequest.setAmount(BigDecimal.valueOf(300.00));
        withdrawalRequest.setType("WITHDRAWAL");

        // When - Process withdrawal
        ResponseEntity<Map> transactionResponse = restTemplate.postForEntity(
                baseUrl + "/accounts/process", withdrawalRequest, Map.class);

        // Then - Verify transaction success
        assertThat(transactionResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = transactionResponse.getBody();
        assertThat(responseBody.get("success")).isEqualTo(true);

        // Verify account balance updated
        ResponseEntity<Account> updatedAccountResponse = restTemplate.getForEntity(
                baseUrl + "/accounts/" + account.getId(), Account.class);
        Account updatedAccount = updatedAccountResponse.getBody();
        assertThat(updatedAccount.getBalance().compareTo(BigDecimal.valueOf(700.00))).isEqualTo(0);
    }

    @Test
    void shouldProcessTransferTransactionE2E() {
        // Given - Create two customers and accounts
        Customer customer1 = createTestCustomer("Customer 1", "customer1@example.com");
        Customer customer2 = createTestCustomer("Customer 2", "customer2@example.com");
        
        Account account1 = createTestAccount(customer1.getId());
        Account account2 = createTestAccount(customer2.getId());
        
        // Set initial balance for account1 using deposit
        ProcessTransactionRequest initialDepositRequest = new ProcessTransactionRequest();
        initialDepositRequest.setTo(account1.getId());
        initialDepositRequest.setAmount(BigDecimal.valueOf(1000.00));
        initialDepositRequest.setType("DEPOSIT");
        
        ResponseEntity<Map> initialDepositResponse = restTemplate.postForEntity(
                baseUrl + "/accounts/process", initialDepositRequest, Map.class);
        assertThat(initialDepositResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ProcessTransactionRequest transferRequest = new ProcessTransactionRequest();
        transferRequest.setFrom(account1.getId());
        transferRequest.setTo(account2.getId());
        transferRequest.setAmount(BigDecimal.valueOf(250.00));
        transferRequest.setType("TRANSFER");

        // When - Process transfer
        ResponseEntity<Map> transactionResponse = restTemplate.postForEntity(
                baseUrl + "/accounts/process", transferRequest, Map.class);

        // Then - Verify transaction success
        assertThat(transactionResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = transactionResponse.getBody();
        assertThat(responseBody.get("success")).isEqualTo(true);

        // Verify both account balances updated
        ResponseEntity<Account> account1Response = restTemplate.getForEntity(
                baseUrl + "/accounts/" + account1.getId(), Account.class);
        Account updatedAccount1 = account1Response.getBody();
        assertThat(updatedAccount1.getBalance().compareTo(BigDecimal.valueOf(750.00))).isEqualTo(0);

        ResponseEntity<Account> account2Response = restTemplate.getForEntity(
                baseUrl + "/accounts/" + account2.getId(), Account.class);
        Account updatedAccount2 = account2Response.getBody();
        assertThat(updatedAccount2.getBalance().compareTo(BigDecimal.valueOf(250.00))).isEqualTo(0);
    }

    @Test
    void shouldHandleInsufficientFundsE2E() {
        // Given - Create customer and account with low balance
        Customer customer = createTestCustomer();
        Account account = createTestAccount(customer.getId());
        
        // Set low balance using deposit
        ProcessTransactionRequest initialDepositRequest = new ProcessTransactionRequest();
        initialDepositRequest.setTo(account.getId());
        initialDepositRequest.setAmount(BigDecimal.valueOf(100.00));
        initialDepositRequest.setType("DEPOSIT");
        
        ResponseEntity<Map> initialDepositResponse = restTemplate.postForEntity(
                baseUrl + "/accounts/process", initialDepositRequest, Map.class);
        assertThat(initialDepositResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ProcessTransactionRequest withdrawalRequest = new ProcessTransactionRequest();
        withdrawalRequest.setFrom(account.getId());
        withdrawalRequest.setAmount(BigDecimal.valueOf(200.00));
        withdrawalRequest.setType("WITHDRAWAL");

        // When - Process withdrawal with insufficient funds
        ResponseEntity<Map> transactionResponse = restTemplate.postForEntity(
                baseUrl + "/accounts/process", withdrawalRequest, Map.class);

        // Then - Verify transaction failure
        assertThat(transactionResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, Object> responseBody = transactionResponse.getBody();
        assertThat(responseBody.get("success")).isEqualTo(false);
        assertThat(responseBody.get("error")).isEqualTo("TRANSACTION_FAILED");

        // Verify account balance unchanged
        ResponseEntity<Account> accountResponse = restTemplate.getForEntity(
                baseUrl + "/accounts/" + account.getId(), Account.class);
        Account unchangedAccount = accountResponse.getBody();
        assertThat(unchangedAccount.getBalance().compareTo(BigDecimal.valueOf(100.00))).isEqualTo(0);
    }

    @Test
    void shouldGetCustomerAccountsE2E() {
        // Given - Create customer with multiple accounts
        Customer customer = createTestCustomer();
        Account checkingAccount = createTestAccount(customer.getId(), "CHECKING");
        Account savingsAccount = createTestAccount(customer.getId(), "SAVINGS");

        // When - Get all accounts for customer
        ResponseEntity<Account[]> accountsResponse = restTemplate.getForEntity(
                baseUrl + "/accounts?customerId=" + customer.getId(), Account[].class);

        // Then - Verify all accounts returned
        assertThat(accountsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Account[] accounts = accountsResponse.getBody();
        assertThat(accounts).hasSize(2);
        assertThat(accounts).extracting(Account::getAccountType)
                .containsExactlyInAnyOrder("CHECKING", "SAVINGS");
        assertThat(accounts).extracting(Account::getCustId)
                .containsOnly(customer.getId());
    }

    @Test
    void shouldGetAccountTransactionsE2E() {
        // Given - Create customer, account, and transactions
        Customer customer = createTestCustomer();
        Account account = createTestAccount(customer.getId());
        
        // Create transactions using API
        ProcessTransactionRequest depositRequest = new ProcessTransactionRequest();
        depositRequest.setTo(account.getId());
        depositRequest.setAmount(BigDecimal.valueOf(100.00));
        depositRequest.setType("DEPOSIT");
        depositRequest.setDetails("Test deposit");
        
        ProcessTransactionRequest withdrawalRequest = new ProcessTransactionRequest();
        withdrawalRequest.setFrom(account.getId());
        withdrawalRequest.setAmount(BigDecimal.valueOf(50.00));
        withdrawalRequest.setType("WITHDRAWAL");
        withdrawalRequest.setDetails("Test withdrawal");
        
        // Execute transactions
        ResponseEntity<Map> depositResponse = restTemplate.postForEntity(
                baseUrl + "/accounts/process", depositRequest, Map.class);
        ResponseEntity<Map> withdrawalResponse = restTemplate.postForEntity(
                baseUrl + "/accounts/process", withdrawalRequest, Map.class);
        
        assertThat(depositResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(withdrawalResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // When - Get transactions for account
        ResponseEntity<Transaction[]> transactionsResponse = restTemplate.getForEntity(
                baseUrl + "/accounts/" + account.getId() + "/transactions", Transaction[].class);

        // Then - Verify transactions returned
        assertThat(transactionsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Transaction[] transactions = transactionsResponse.getBody();
        assertThat(transactions).hasSize(2);
        assertThat(transactions).extracting(Transaction::getType)
                .containsExactlyInAnyOrder("DEPOSIT", "WITHDRAWAL");
    }

    @Test
    void shouldHandleInvalidAccountIdE2E() {
        // When - Try to get non-existent account
        ResponseEntity<Account> response = restTemplate.getForEntity(
                baseUrl + "/accounts/99999", Account.class);

        // Then - Verify null response (as per current implementation)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void shouldHandleInvalidCustomerIdE2E() {
        // When - Try to get accounts for non-existent customer
        ResponseEntity<Account[]> response = restTemplate.getForEntity(
                baseUrl + "/accounts?customerId=99999", Account[].class);

        // Then - Verify empty array
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Account[] accounts = response.getBody();
        assertThat(accounts).isEmpty();
    }

    @Test
    void shouldProcessInternationalTransferWithFeesE2E() {
        // Given - Create two customers and accounts
        Customer customer1 = createTestCustomer("International Customer 1", "intl1@example.com");
        Customer customer2 = createTestCustomer("International Customer 2", "intl2@example.com");
        
        Account account1 = createTestAccount(customer1.getId());
        Account account2 = createTestAccount(customer2.getId());
        
        // Set sufficient balance for transfer + fees using deposit
        ProcessTransactionRequest initialDepositRequest = new ProcessTransactionRequest();
        initialDepositRequest.setTo(account1.getId());
        initialDepositRequest.setAmount(BigDecimal.valueOf(1000.00));
        initialDepositRequest.setType("DEPOSIT");
        
        ResponseEntity<Map> initialDepositResponse = restTemplate.postForEntity(
                baseUrl + "/accounts/process", initialDepositRequest, Map.class);
        assertThat(initialDepositResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ProcessTransactionRequest internationalTransferRequest = new ProcessTransactionRequest();
        internationalTransferRequest.setFrom(account1.getId());
        internationalTransferRequest.setTo(account2.getId());
        internationalTransferRequest.setAmount(BigDecimal.valueOf(200.00));
        internationalTransferRequest.setType("INTERNATIONAL_TRANSFER");

        // When - Process international transfer
        ResponseEntity<Map> transactionResponse = restTemplate.postForEntity(
                baseUrl + "/accounts/process", internationalTransferRequest, Map.class);

        // Then - Verify transaction success
        assertThat(transactionResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = transactionResponse.getBody();
        assertThat(responseBody.get("success")).isEqualTo(true);

        // Verify account balances (200 transfer + 50 fee = 250 total from account1)
        ResponseEntity<Account> account1Response = restTemplate.getForEntity(
                baseUrl + "/accounts/" + account1.getId(), Account.class);
        Account updatedAccount1 = account1Response.getBody();
        assertThat(updatedAccount1.getBalance().compareTo(BigDecimal.valueOf(750.00))).isEqualTo(0);

        ResponseEntity<Account> account2Response = restTemplate.getForEntity(
                baseUrl + "/accounts/" + account2.getId(), Account.class);
        Account updatedAccount2 = account2Response.getBody();
        assertThat(updatedAccount2.getBalance().compareTo(BigDecimal.valueOf(200.00))).isEqualTo(0);
    }

    // Helper methods
    private Customer createTestCustomer() {
        return createTestCustomer("E2E Test Customer " + System.currentTimeMillis(), 
                                 "e2e" + System.currentTimeMillis() + "@example.com");
    }

    private Customer createTestCustomer(String name, String email) {
        CustomerCreationRequest customerRequest = new CustomerCreationRequest();
        customerRequest.setName(name);
        customerRequest.setEmail(email);
        customerRequest.setPhone("1234567890");

        ResponseEntity<Customer> response = restTemplate.postForEntity(
                baseUrl + "/customers", customerRequest, Customer.class);
        
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("Failed to create test customer: " + response.getStatusCode());
        }
        
        return response.getBody();
    }

    private Account createTestAccount(Long customerId) {
        return createTestAccount(customerId, "CHECKING");
    }

    private Account createTestAccount(Long customerId, String accountType) {
        AccountCreationRequest accountRequest = new AccountCreationRequest();
        accountRequest.setCustomerId(customerId);
        accountRequest.setAccountType(accountType);

        ResponseEntity<Account> response = restTemplate.postForEntity(
                baseUrl + "/accounts", accountRequest, Account.class);
        return response.getBody();
    }
}
