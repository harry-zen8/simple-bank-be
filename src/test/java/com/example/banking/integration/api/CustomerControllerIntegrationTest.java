package com.example.banking.integration.api;

import com.example.banking.config.SecurityConfig;
import com.example.banking.controller.AccountController;
import com.example.banking.model.Customer;
import com.example.banking.model.CustomerCreationRequest;
import com.example.banking.service.AccountService;
import com.example.banking.service.CustomerService;
import com.example.banking.service.FeeCalculationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@ContextConfiguration(classes = {AccountController.class, SecurityConfig.class})
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private FeeCalculationService feeCalculationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateCustomerWithValidData() throws Exception {
        // Given
        CustomerCreationRequest request = new CustomerCreationRequest();
        request.setName("John Doe");
        request.setEmail("john.doe@example.com");
        request.setPhone("1234567890");

        Customer expectedCustomer = new Customer();
        expectedCustomer.setId(1L);
        expectedCustomer.setName("John Doe");
        expectedCustomer.setEmail("john.doe@example.com");
        expectedCustomer.setPhone("1234567890");
        expectedCustomer.setCustomerLevel("BRONZE");

        when(customerService.createCustomer(any(CustomerCreationRequest.class)))
                .thenReturn(expectedCustomer);

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.phone").value("1234567890"))
                .andExpect(jsonPath("$.customerLevel").value("BRONZE"));
    }

    @Test
    void shouldCreateCustomerWithMinimalData() throws Exception {
        // Given
        CustomerCreationRequest request = new CustomerCreationRequest();
        request.setName("Jane Smith");

        Customer expectedCustomer = new Customer();
        expectedCustomer.setId(2L);
        expectedCustomer.setName("Jane Smith");
        expectedCustomer.setEmail(null);
        expectedCustomer.setPhone(null);
        expectedCustomer.setCustomerLevel(null);

        when(customerService.createCustomer(any(CustomerCreationRequest.class)))
                .thenReturn(expectedCustomer);

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Jane Smith"))
                .andExpect(jsonPath("$.email").isEmpty())
                .andExpect(jsonPath("$.phone").isEmpty())
                .andExpect(jsonPath("$.customerLevel").isEmpty());
    }

    @Test
    void shouldGetCustomerByIdSuccessfully() throws Exception {
        // Given
        Customer expectedCustomer = new Customer();
        expectedCustomer.setId(1L);
        expectedCustomer.setName("Test Customer");
        expectedCustomer.setEmail("test@example.com");
        expectedCustomer.setPhone("1234567890");
        expectedCustomer.setCustomerLevel("STANDARD");

        when(customerService.getCustomer(1L)).thenReturn(expectedCustomer);

        // When & Then
        mockMvc.perform(get("/api/v1/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Customer"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.phone").value("1234567890"))
                .andExpect(jsonPath("$.customerLevel").value("STANDARD"));
    }

    @Test
    void shouldGetAllCustomersSuccessfully() throws Exception {
        // Given
        Customer customer1 = new Customer();
        customer1.setId(1L);
        customer1.setName("Customer One");
        customer1.setEmail("customer1@example.com");
        customer1.setPhone("1111111111");
        customer1.setCustomerLevel("BASIC");

        Customer customer2 = new Customer();
        customer2.setId(2L);
        customer2.setName("Customer Two");
        customer2.setEmail("customer2@example.com");
        customer2.setPhone("2222222222");
        customer2.setCustomerLevel("PREMIUM");

        List<Customer> customers = Arrays.asList(customer1, customer2);

        when(customerService.getAllCustomers()).thenReturn(customers);

        // When & Then
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Customer One"))
                .andExpect(jsonPath("$[0].email").value("customer1@example.com"))
                .andExpect(jsonPath("$[0].phone").value("1111111111"))
                .andExpect(jsonPath("$[0].customerLevel").value("BASIC"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Customer Two"))
                .andExpect(jsonPath("$[1].email").value("customer2@example.com"))
                .andExpect(jsonPath("$[1].phone").value("2222222222"))
                .andExpect(jsonPath("$[1].customerLevel").value("PREMIUM"));
    }

    @Test
    void shouldReturnEmptyListWhenNoCustomers() throws Exception {
        // Given
        when(customerService.getAllCustomers()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldHandleCustomerNotFound() throws Exception {
        // Given
        when(customerService.getCustomer(999L)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/v1/customers/999"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void shouldHandleInvalidCustomerId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/customers/invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleInvalidJsonInCustomerCreation() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleMissingContentTypeInCustomerCreation() throws Exception {
        // Given
        CustomerCreationRequest request = new CustomerCreationRequest();
        request.setName("Test Customer");

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void shouldHandleCustomerCreationWithLongName() throws Exception {
        // Given
        CustomerCreationRequest request = new CustomerCreationRequest();
        request.setName("A".repeat(300)); // Exceeds typical database limits
        request.setEmail("longname@example.com");

        Customer expectedCustomer = new Customer();
        expectedCustomer.setId(1L);
        expectedCustomer.setName("A".repeat(300));
        expectedCustomer.setEmail("longname@example.com");

        when(customerService.createCustomer(any(CustomerCreationRequest.class)))
                .thenReturn(expectedCustomer);

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("A".repeat(300)));
    }

    @Test
    void shouldHandleCustomerCreationWithSpecialCharacters() throws Exception {
        // Given
        CustomerCreationRequest request = new CustomerCreationRequest();
        request.setName("José María O'Connor-Smith");
        request.setEmail("jose.maria@example.com");
        request.setPhone("+1-555-123-4567");

        Customer expectedCustomer = new Customer();
        expectedCustomer.setId(1L);
        expectedCustomer.setName("José María O'Connor-Smith");
        expectedCustomer.setEmail("jose.maria@example.com");
        expectedCustomer.setPhone("+1-555-123-4567");

        when(customerService.createCustomer(any(CustomerCreationRequest.class)))
                .thenReturn(expectedCustomer);

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("José María O'Connor-Smith"))
                .andExpect(jsonPath("$.email").value("jose.maria@example.com"))
                .andExpect(jsonPath("$.phone").value("+1-555-123-4567"));
    }

    @Test
    void shouldHandleCustomerCreationWithEmptyName() throws Exception {
        // Given
        CustomerCreationRequest request = new CustomerCreationRequest();
        request.setName("");
        request.setEmail("empty@example.com");

        // When & Then - This should still work as validation is handled by service layer
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldHandleCustomerCreationWithNullFields() throws Exception {
        // Given
        CustomerCreationRequest request = new CustomerCreationRequest();
        // All fields are null

        Customer expectedCustomer = new Customer();
        expectedCustomer.setId(1L);
        expectedCustomer.setName(null);
        expectedCustomer.setEmail(null);
        expectedCustomer.setPhone(null);
        expectedCustomer.setCustomerLevel(null);

        when(customerService.createCustomer(any(CustomerCreationRequest.class)))
                .thenReturn(expectedCustomer);

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").isEmpty())
                .andExpect(jsonPath("$.email").isEmpty())
                .andExpect(jsonPath("$.phone").isEmpty())
                .andExpect(jsonPath("$.customerLevel").isEmpty());
    }
}
