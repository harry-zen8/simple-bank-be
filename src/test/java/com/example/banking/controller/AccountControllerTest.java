package com.example.banking.controller;

import com.example.banking.model.Account;
import com.example.banking.model.AccountCreationRequest;
import com.example.banking.model.Customer;
import com.example.banking.model.CustomerCreationRequest;
import com.example.banking.model.ProcessTransactionRequest;
import com.example.banking.model.Transaction;
import com.example.banking.service.AccountService;
import com.example.banking.service.CustomerService;
import com.example.banking.service.FeeCalculationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @Mock
    private CustomerService customerService;

    @Mock
    private FeeCalculationService feeCalculationService;

    @InjectMocks
    private AccountController accountController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
        objectMapper = new ObjectMapper();
    }

    // Customer Endpoints Tests

    @Test
    void testCreateCustomer_success() throws Exception {
        // ARRANGE
        CustomerCreationRequest request = new CustomerCreationRequest();
        request.setName("John Doe");

        Customer expectedCustomer = new Customer();
        expectedCustomer.setId(1L);
        expectedCustomer.setName("John Doe");
        expectedCustomer.setCustomerLevel("BRONZE");

        when(customerService.createCustomer(any(CustomerCreationRequest.class)))
            .thenReturn(expectedCustomer);

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.customerLevel").value("BRONZE"));
    }

    @Test
    void testGetCustomer_success() throws Exception {
        // ARRANGE
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");
        customer.setCustomerLevel("SILVER");

        when(customerService.getCustomer(1L)).thenReturn(customer);

        // ACT & ASSERT
        mockMvc.perform(get("/api/v1/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.customerLevel").value("SILVER"));
    }

    @Test
    void testGetAllCustomers_success() throws Exception {
        // ARRANGE
        Customer customer1 = new Customer();
        customer1.setId(1L);
        customer1.setName("John Doe");

        Customer customer2 = new Customer();
        customer2.setId(2L);
        customer2.setName("Jane Smith");

        List<Customer> customers = Arrays.asList(customer1, customer2);
        when(customerService.getAllCustomers()).thenReturn(customers);

        // ACT & ASSERT
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Jane Smith"));
    }

    // Account Endpoints Tests

    @Test
    void testCreateAccount_success() throws Exception {
        // ARRANGE
        AccountCreationRequest request = new AccountCreationRequest();
        request.setCustomerId(1L);
        request.setAccountType("SAVINGS");

        Account expectedAccount = new Account();
        expectedAccount.setId(1L);
        expectedAccount.setCustId(1L);
        expectedAccount.setAccountType("SAVINGS");
        expectedAccount.setBalance(BigDecimal.ZERO);

        when(accountService.createAccount(any(AccountCreationRequest.class)))
            .thenReturn(expectedAccount);

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.custId").value(1))
                .andExpect(jsonPath("$.accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    void testGetAccount_success() throws Exception {
        // ARRANGE
        Account account = new Account();
        account.setId(1L);
        account.setCustId(1L);
        account.setAccountType("CHECKING");
        account.setBalance(new BigDecimal("1000"));

        when(accountService.getAccount(1L)).thenReturn(account);

        // ACT & ASSERT
        mockMvc.perform(get("/api/v1/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.custId").value(1))
                .andExpect(jsonPath("$.accountType").value("CHECKING"))
                .andExpect(jsonPath("$.balance").value(1000));
    }

    @Test
    void testGetAccountsByCustomer_success() throws Exception {
        // ARRANGE
        Account account1 = new Account();
        account1.setId(1L);
        account1.setCustId(1L);
        account1.setAccountType("CHECKING");

        Account account2 = new Account();
        account2.setId(2L);
        account2.setCustId(1L);
        account2.setAccountType("SAVINGS");

        List<Account> accounts = Arrays.asList(account1, account2);
        when(accountService.getAccountsByCustomerId(1L)).thenReturn(accounts);

        // ACT & ASSERT
        mockMvc.perform(get("/api/v1/accounts")
                .param("customerId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].accountType").value("CHECKING"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].accountType").value("SAVINGS"));
    }

    @Test
    void testGetTransactions_success() throws Exception {
        // ARRANGE
        Transaction transaction1 = new Transaction();
        transaction1.setId(1L);
        transaction1.setFromAccount(1L);
        transaction1.setToAccount(2L);
        transaction1.setValue(new BigDecimal("100"));
        transaction1.setType("TRANSFER");

        Transaction transaction2 = new Transaction();
        transaction2.setId(2L);
        transaction2.setToAccount(1L);
        transaction2.setValue(new BigDecimal("50"));
        transaction2.setType("DEPOSIT");

        List<Transaction> transactions = Arrays.asList(transaction1, transaction2);
        when(accountService.getTransactions(1L)).thenReturn(transactions);

        // ACT & ASSERT
        mockMvc.perform(get("/api/v1/accounts/1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].type").value("TRANSFER"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].type").value("DEPOSIT"));
    }

    // Transaction Processing Tests

    @Test
    void testProcessTransaction_success() throws Exception {
        // ARRANGE
        ProcessTransactionRequest request = new ProcessTransactionRequest();
        request.setFrom(1L);
        request.setTo(2L);
        request.setAmount(new BigDecimal("100"));
        request.setType("TRANSFER");

        when(accountService.processTransaction(any(ProcessTransactionRequest.class)))
            .thenReturn(true);

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/accounts/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Transaction processed successfully"));
    }

    @Test
    void testProcessTransaction_failure() throws Exception {
        // ARRANGE
        ProcessTransactionRequest request = new ProcessTransactionRequest();
        request.setFrom(1L);
        request.setTo(2L);
        request.setAmount(new BigDecimal("100"));
        request.setType("TRANSFER");

        when(accountService.processTransaction(any(ProcessTransactionRequest.class)))
            .thenReturn(false);

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/accounts/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("TRANSACTION_FAILED"))
                .andExpect(jsonPath("$.message").value("Transaction failed. Please check the server logs for details."));
    }

    @Test
    void testProcessTransaction_balanceLimitExceeded() throws Exception {
        // ARRANGE
        ProcessTransactionRequest request = new ProcessTransactionRequest();
        request.setTo(1L);
        request.setAmount(new BigDecimal("1000"));
        request.setType("DEPOSIT");

        when(accountService.processTransaction(any(ProcessTransactionRequest.class)))
            .thenThrow(new IllegalArgumentException("Student account balance cannot exceed $10,000"));

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/accounts/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("BALANCE_LIMIT_EXCEEDED"))
                .andExpect(jsonPath("$.message").value("Student account balance cannot exceed $10,000"))
                .andExpect(jsonPath("$.lspViolation").value(true));
    }

    // Fee Calculation Tests

    @Test
    void testCalculateFees_success() throws Exception {
        // ARRANGE
        String expectedResult = "Charged $10.00 fee to account 1 (Monthly account fee)";
        when(feeCalculationService.handle(1L)).thenReturn(expectedResult);

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/accounts/1/fees"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResult));
    }

    @Test
    void testCalculateFees_exception() throws Exception {
        // ARRANGE
        when(feeCalculationService.handle(1L))
            .thenThrow(new RuntimeException("Database connection failed"));

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/accounts/1/fees"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An error occurred during fee calculation: Database connection failed"));
    }

    // Edge Cases and Error Handling Tests

    @Test
    void testCreateAccount_invalidJson() throws Exception {
        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAccount_notFound() throws Exception {
        // ARRANGE
        when(accountService.getAccount(999L)).thenReturn(null);

        // ACT & ASSERT
        mockMvc.perform(get("/api/v1/accounts/999"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void testProcessTransaction_missingRequiredFields() throws Exception {
        // ARRANGE
        ProcessTransactionRequest request = new ProcessTransactionRequest();
        // Missing required fields

        when(accountService.processTransaction(any(ProcessTransactionRequest.class)))
            .thenReturn(false);

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/accounts/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testGetAccountsByCustomer_missingParameter() throws Exception {
        // ACT & ASSERT
        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testProcessTransaction_largeAmount() throws Exception {
        // ARRANGE
        ProcessTransactionRequest request = new ProcessTransactionRequest();
        request.setFrom(1L);
        request.setAmount(new BigDecimal("50000"));
        request.setType("WITHDRAWAL");

        when(accountService.processTransaction(any(ProcessTransactionRequest.class)))
            .thenReturn(true);

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/accounts/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testProcessTransaction_internationalTransfer() throws Exception {
        // ARRANGE
        ProcessTransactionRequest request = new ProcessTransactionRequest();
        request.setFrom(1L);
        request.setTo(2L);
        request.setAmount(new BigDecimal("1000"));
        request.setType("INTERNATIONAL_TRANSFER");

        when(accountService.processTransaction(any(ProcessTransactionRequest.class)))
            .thenReturn(true);

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/accounts/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
