package com.example.banking.service;

import com.example.banking.model.Customer;
import com.example.banking.model.CustomerCreationRequest;
import com.example.banking.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void testCreateCustomer_success() {
        CustomerCreationRequest request = new CustomerCreationRequest();
        request.setName("John Doe");

        Customer customer = new Customer();
        customer.setName("John Doe");
        customer.setCustomerLevel("BRONZE");

        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        Customer createdCustomer = customerService.createCustomer(request);

        assertEquals("John Doe", createdCustomer.getName());
        assertEquals("BRONZE", createdCustomer.getCustomerLevel());
    }

    @Test
    void testCreateCustomer_noName() {
        CustomerCreationRequest request = new CustomerCreationRequest();
        Customer createdCustomer = customerService.createCustomer(request);
        assertNull(createdCustomer);
    }

    @Test
    void testGetCustomer_found() {
        Customer customer = new Customer();
        customer.setId(1L);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        Customer foundCustomer = customerService.getCustomer(1L);
        assertEquals(customer, foundCustomer);
    }

    @Test
    void testGetCustomer_notFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        // This will throw an exception, let's assert that
        assertThrows(java.util.NoSuchElementException.class, () -> {
            customerService.getCustomer(1L);
        });
    }

    @Test
    void testGetAllCustomers() {
        Customer customer1 = new Customer();
        customer1.setId(1L);
        Customer customer2 = new Customer();
        customer2.setId(2L);
        when(customerRepository.findAll()).thenReturn(java.util.List.of(customer1, customer2));
        java.util.List<Customer> customers = customerService.getAllCustomers();
        assertEquals(2, customers.size());
    }
} 