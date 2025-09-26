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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class TransactionE2ETest {

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
    void shouldProcessCompleteTransactionWorkflowE2E() {
        // Given - Create two customers
        Customer alice = createCustomer("Alice", "alice@example.com");
        Customer bob = createCustomer("Bob", "bob@example.com");

        // Create accounts for both customers
        Account aliceAccount = createAccount(alice.getId(), "CHECKING");
        Account bobAccount = createAccount(bob.getId(), "SAVINGS");

        // Set initial balances using deposits
        ProcessTransactionRequest aliceDepositRequest = new ProcessTransactionRequest();
        aliceDepositRequest.setTo(aliceAccount.getId());
        aliceDepositRequest.setAmount(BigDecimal.valueOf(1000.00));
        aliceDepositRequest.setType("DEPOSIT");
        
        ProcessTransactionRequest bobDepositRequest = new ProcessTransactionRequest();
        bobDepositRequest.setTo(bobAccount.getId());
        bobDepositRequest.setAmount(BigDecimal.valueOf(500.00));
        bobDepositRequest.setType("DEPOSIT");
        
        ResponseEntity<Map> aliceDepositResponse = restTemplate.postForEntity(
                baseUrl + "/accounts/process", aliceDepositRequest, Map.class);
        ResponseEntity<Map> bobDepositResponse = restTemplate.postForEntity(
                baseUrl + "/accounts/process", bobDepositRequest, Map.class);
        
        assertThat(aliceDepositResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(bobDepositResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // When - Alice deposits money into her account
        ProcessTransactionRequest depositRequest = new ProcessTransactionRequest();
        depositRequest.setTo(aliceAccount.getId());
        depositRequest.setAmount(BigDecimal.valueOf(200.00));
        depositRequest.setType("DEPOSIT");

        ResponseEntity<Map> depositResponse = restTemplate.postForEntity(
                baseUrl + "/accounts/process", depositRequest, Map.class);

        // Then - Verify deposit success
        assertThat(depositResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(depositResponse.getBody().get("success")).isEqualTo(true);

        // Verify Alice's balance increased
        Account updatedAliceAccount = getAccount(aliceAccount.getId());
        assertThat(updatedAliceAccount.getBalance().compareTo(BigDecimal.valueOf(1200.00))).isEqualTo(0);

        // When - Alice transfers money to Bob
        ProcessTransactionRequest transferRequest = new ProcessTransactionRequest();
        transferRequest.setFrom(aliceAccount.getId());
        transferRequest.setTo(bobAccount.getId());
        transferRequest.setAmount(BigDecimal.valueOf(300.00));
        transferRequest.setType("TRANSFER");

        ResponseEntity<Map> transferResponse = restTemplate.postForEntity(
                baseUrl + "/accounts/process", transferRequest, Map.class);

        // Then - Verify transfer success
        assertThat(transferResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(transferResponse.getBody().get("success")).isEqualTo(true);

        // Verify both account balances updated correctly
        Account finalAliceAccount = getAccount(aliceAccount.getId());
        Account finalBobAccount = getAccount(bobAccount.getId());
        
        assertThat(finalAliceAccount.getBalance().compareTo(BigDecimal.valueOf(900.00))).isEqualTo(0);
        assertThat(finalBobAccount.getBalance().compareTo(BigDecimal.valueOf(800.00))).isEqualTo(0);

        // When - Bob withdraws money
        ProcessTransactionRequest withdrawalRequest = new ProcessTransactionRequest();
        withdrawalRequest.setFrom(bobAccount.getId());
        withdrawalRequest.setAmount(BigDecimal.valueOf(100.00));
        withdrawalRequest.setType("WITHDRAWAL");

        ResponseEntity<Map> withdrawalResponse = restTemplate.postForEntity(
                baseUrl + "/accounts/process", withdrawalRequest, Map.class);

        // Then - Verify withdrawal success
        assertThat(withdrawalResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(withdrawalResponse.getBody().get("success")).isEqualTo(true);

        // Verify Bob's final balance
        Account finalBobAccountAfterWithdrawal = getAccount(bobAccount.getId());
        assertThat(finalBobAccountAfterWithdrawal.getBalance().compareTo(BigDecimal.valueOf(700.00))).isEqualTo(0);
    }

    @Test
    void shouldHandleMultipleConcurrentTransactionsE2E() {
        // Given - Create customer with account
        Customer customer = createCustomer("Concurrent Customer", "concurrent@example.com");
        Account account = createAccount(customer.getId(), "CHECKING");
        
        // Set initial balance using deposit
        ProcessTransactionRequest initialDepositRequest = new ProcessTransactionRequest();
        initialDepositRequest.setTo(account.getId());
        initialDepositRequest.setAmount(BigDecimal.valueOf(1000.00));
        initialDepositRequest.setType("DEPOSIT");
        
        ResponseEntity<Map> initialDepositResponse = restTemplate.postForEntity(
                baseUrl + "/accounts/process", initialDepositRequest, Map.class);
        assertThat(initialDepositResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // When - Process multiple deposits concurrently
        ProcessTransactionRequest deposit1 = new ProcessTransactionRequest();
        deposit1.setTo(account.getId());
        deposit1.setAmount(BigDecimal.valueOf(100.00));
        deposit1.setType("DEPOSIT");

        ProcessTransactionRequest deposit2 = new ProcessTransactionRequest();
        deposit2.setTo(account.getId());
        deposit2.setAmount(BigDecimal.valueOf(200.00));
        deposit2.setType("DEPOSIT");

        ProcessTransactionRequest deposit3 = new ProcessTransactionRequest();
        deposit3.setTo(account.getId());
        deposit3.setAmount(BigDecimal.valueOf(150.00));
        deposit3.setType("DEPOSIT");

        // Process all deposits
        ResponseEntity<Map> response1 = restTemplate.postForEntity(
                baseUrl + "/accounts/process", deposit1, Map.class);
        ResponseEntity<Map> response2 = restTemplate.postForEntity(
                baseUrl + "/accounts/process", deposit2, Map.class);
        ResponseEntity<Map> response3 = restTemplate.postForEntity(
                baseUrl + "/accounts/process", deposit3, Map.class);

        // Then - Verify all transactions succeeded
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify final balance (1000 + 100 + 200 + 150 = 1450)
        Account finalAccount = getAccount(account.getId());
        assertThat(finalAccount.getBalance().compareTo(BigDecimal.valueOf(1450.00))).isEqualTo(0);
    }

    @Test
    void shouldHandleTransactionFailureScenariosE2E() {
        // Given - Create customer with account having low balance
        Customer customer = createCustomer("Low Balance Customer", "lowbalance@example.com");
        Account account = createAccount(customer.getId(), "CHECKING");
        
        // Set low balance using deposit
        ProcessTransactionRequest initialDepositRequest = new ProcessTransactionRequest();
        initialDepositRequest.setTo(account.getId());
        initialDepositRequest.setAmount(BigDecimal.valueOf(50.00));
        initialDepositRequest.setType("DEPOSIT");
        
        ResponseEntity<Map> initialDepositResponse = restTemplate.postForEntity(
                baseUrl + "/accounts/process", initialDepositRequest, Map.class);
        assertThat(initialDepositResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // When - Try to withdraw more than available balance
        ProcessTransactionRequest withdrawalRequest = new ProcessTransactionRequest();
        withdrawalRequest.setFrom(account.getId());
        withdrawalRequest.setAmount(BigDecimal.valueOf(100.00));
        withdrawalRequest.setType("WITHDRAWAL");

        ResponseEntity<Map> withdrawalResponse = restTemplate.postForEntity(
                baseUrl + "/accounts/process", withdrawalRequest, Map.class);

        // Then - Verify withdrawal failed
        assertThat(withdrawalResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(withdrawalResponse.getBody().get("success")).isEqualTo(false);
        assertThat(withdrawalResponse.getBody().get("error")).isEqualTo("TRANSACTION_FAILED");

        // Verify account balance unchanged
        Account unchangedAccount = getAccount(account.getId());
        assertThat(unchangedAccount.getBalance().compareTo(BigDecimal.valueOf(50.00))).isEqualTo(0);

        // When - Try to transfer to non-existent account
        ProcessTransactionRequest transferRequest = new ProcessTransactionRequest();
        transferRequest.setFrom(account.getId());
        transferRequest.setTo(99999L); // Non-existent account
        transferRequest.setAmount(BigDecimal.valueOf(25.00));
        transferRequest.setType("TRANSFER");

        ResponseEntity<Map> transferResponse = restTemplate.postForEntity(
                baseUrl + "/accounts/process", transferRequest, Map.class);

        // Then - Verify transfer failed
        assertThat(transferResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(transferResponse.getBody().get("success")).isEqualTo(false);
    }

    @Test
    void shouldProcessInternationalTransferWithFeesE2E() {
        // Given - Create two customers with accounts
        Customer sender = createCustomer("International Sender", "sender@example.com");
        Customer receiver = createCustomer("International Receiver", "receiver@example.com");
        
        Account senderAccount = createAccount(sender.getId(), "CHECKING");
        Account receiverAccount = createAccount(receiver.getId(), "SAVINGS");
        
        // Set sufficient balance for transfer + fees using deposit
        ProcessTransactionRequest initialDepositRequest = new ProcessTransactionRequest();
        initialDepositRequest.setTo(senderAccount.getId());
        initialDepositRequest.setAmount(BigDecimal.valueOf(1000.00));
        initialDepositRequest.setType("DEPOSIT");
        
        ResponseEntity<Map> initialDepositResponse = restTemplate.postForEntity(
                baseUrl + "/accounts/process", initialDepositRequest, Map.class);
        assertThat(initialDepositResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // When - Process international transfer
        ProcessTransactionRequest internationalTransferRequest = new ProcessTransactionRequest();
        internationalTransferRequest.setFrom(senderAccount.getId());
        internationalTransferRequest.setTo(receiverAccount.getId());
        internationalTransferRequest.setAmount(BigDecimal.valueOf(300.00));
        internationalTransferRequest.setType("INTERNATIONAL_TRANSFER");

        ResponseEntity<Map> transferResponse = restTemplate.postForEntity(
                baseUrl + "/accounts/process", internationalTransferRequest, Map.class);

        // Then - Verify transfer success
        assertThat(transferResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(transferResponse.getBody().get("success")).isEqualTo(true);

        // Verify balances: sender loses 300 + 50 fee = 350, receiver gains 300
        Account finalSenderAccount = getAccount(senderAccount.getId());
        Account finalReceiverAccount = getAccount(receiverAccount.getId());
        
        assertThat(finalSenderAccount.getBalance().compareTo(BigDecimal.valueOf(650.00))).isEqualTo(0);
        assertThat(finalReceiverAccount.getBalance().compareTo(BigDecimal.valueOf(300.00))).isEqualTo(0);
    }

    @Test
    void shouldHandleInvalidTransactionTypesE2E() {
        // Given - Create customer and account
        Customer customer = createCustomer("Invalid Transaction Customer", "invalid@example.com");
        Account account = createAccount(customer.getId(), "CHECKING");
        
        // Set initial balance using deposit
        ProcessTransactionRequest initialDepositRequest = new ProcessTransactionRequest();
        initialDepositRequest.setTo(account.getId());
        initialDepositRequest.setAmount(BigDecimal.valueOf(1000.00));
        initialDepositRequest.setType("DEPOSIT");
        
        ResponseEntity<Map> initialDepositResponse = restTemplate.postForEntity(
                baseUrl + "/accounts/process", initialDepositRequest, Map.class);
        assertThat(initialDepositResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // When - Try to process transaction with invalid type
        ProcessTransactionRequest invalidRequest = new ProcessTransactionRequest();
        invalidRequest.setFrom(account.getId());
        invalidRequest.setAmount(BigDecimal.valueOf(100.00));
        invalidRequest.setType("INVALID_TYPE");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/accounts/process", invalidRequest, Map.class);

        // Then - Verify transaction failed
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("success")).isEqualTo(false);
        assertThat(response.getBody().get("error")).isEqualTo("TRANSACTION_FAILED");

        // Verify account balance unchanged
        Account unchangedAccount = getAccount(account.getId());
        assertThat(unchangedAccount.getBalance().compareTo(BigDecimal.valueOf(1000.00))).isEqualTo(0);
    }

    @Test
    void shouldHandleZeroAmountTransactionsE2E() {
        // Given - Create customer and account
        Customer customer = createCustomer("Zero Amount Customer", "zero@example.com");
        Account account = createAccount(customer.getId(), "CHECKING");
        account.setBalance(BigDecimal.valueOf(1000.00));
        accountRepository.save(account);

        // When - Try to process transaction with zero amount
        ProcessTransactionRequest zeroAmountRequest = new ProcessTransactionRequest();
        zeroAmountRequest.setFrom(account.getId());
        zeroAmountRequest.setAmount(BigDecimal.ZERO);
        zeroAmountRequest.setType("WITHDRAWAL");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/accounts/process", zeroAmountRequest, Map.class);

        // Then - Verify transaction failed
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("success")).isEqualTo(false);
    }

    @Test
    void shouldHandleNegativeAmountTransactionsE2E() {
        // Given - Create customer and account
        Customer customer = createCustomer("Negative Amount Customer", "negative@example.com");
        Account account = createAccount(customer.getId(), "CHECKING");
        account.setBalance(BigDecimal.valueOf(1000.00));
        accountRepository.save(account);

        // When - Try to process transaction with negative amount
        ProcessTransactionRequest negativeAmountRequest = new ProcessTransactionRequest();
        negativeAmountRequest.setFrom(account.getId());
        negativeAmountRequest.setAmount(BigDecimal.valueOf(-100.00));
        negativeAmountRequest.setType("WITHDRAWAL");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/accounts/process", negativeAmountRequest, Map.class);

        // Then - Verify transaction failed
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("success")).isEqualTo(false);
    }

    @Test
    void shouldProcessLargeAmountTransactionsE2E() {
        // Given - Create customer with account having large balance
        Customer customer = createCustomer("Large Amount Customer", "large@example.com");
        Account account = createAccount(customer.getId(), "SAVINGS");
        
        // Set large balance using deposit
        ProcessTransactionRequest initialDepositRequest = new ProcessTransactionRequest();
        initialDepositRequest.setTo(account.getId());
        initialDepositRequest.setAmount(new BigDecimal("999999999.99"));
        initialDepositRequest.setType("DEPOSIT");
        
        ResponseEntity<Map> initialDepositResponse = restTemplate.postForEntity(
                baseUrl + "/accounts/process", initialDepositRequest, Map.class);
        assertThat(initialDepositResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // When - Process large withdrawal
        ProcessTransactionRequest largeWithdrawalRequest = new ProcessTransactionRequest();
        largeWithdrawalRequest.setFrom(account.getId());
        largeWithdrawalRequest.setAmount(new BigDecimal("1000000.00"));
        largeWithdrawalRequest.setType("WITHDRAWAL");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/accounts/process", largeWithdrawalRequest, Map.class);

        // Then - Verify transaction success
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("success")).isEqualTo(true);

        // Verify balance updated correctly
        Account updatedAccount = getAccount(account.getId());
        assertThat(updatedAccount.getBalance().compareTo(new BigDecimal("998999999.99"))).isEqualTo(0);
    }

    // Helper methods
    private Customer createCustomer(String name, String email) {
        CustomerCreationRequest customerRequest = new CustomerCreationRequest();
        customerRequest.setName(name + " " + System.currentTimeMillis());
        customerRequest.setEmail(email.replace("@", System.currentTimeMillis() + "@"));
        customerRequest.setPhone("1234567890");

        ResponseEntity<Customer> response = restTemplate.postForEntity(
                baseUrl + "/customers", customerRequest, Customer.class);
        
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("Failed to create test customer: " + response.getStatusCode());
        }
        
        return response.getBody();
    }

    private Account createAccount(Long customerId, String accountType) {
        AccountCreationRequest accountRequest = new AccountCreationRequest();
        accountRequest.setCustomerId(customerId);
        accountRequest.setAccountType(accountType);

        ResponseEntity<Account> response = restTemplate.postForEntity(
                baseUrl + "/accounts", accountRequest, Account.class);
        return response.getBody();
    }

    private Account getAccount(Long accountId) {
        ResponseEntity<Account> response = restTemplate.getForEntity(
                baseUrl + "/accounts/" + accountId, Account.class);
        return response.getBody();
    }
}
