package com.example.banking.bdd;

import com.example.banking.model.Customer;
import com.example.banking.model.CustomerCreationRequest;
import com.example.banking.repository.CustomerRepository;
import com.example.banking.service.CustomerService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerManagementStepDefinitions {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer lastCreatedCustomer;

    @Given("a customer {string} already exists")
    public void a_customer_already_exists(String customerName) {
        CustomerCreationRequest customerRequest = new CustomerCreationRequest();
        customerRequest.setName(customerName);
        customerService.createCustomer(customerRequest);
    }

    @When("a new customer is created with the name {string}")
    public void a_new_customer_is_created_with_the_name(String customerName) {
        CustomerCreationRequest customerRequest = new CustomerCreationRequest();
        customerRequest.setName(customerName);
        try {
            lastCreatedCustomer = customerService.createCustomer(customerRequest);
        } catch (Exception e) {
            lastCreatedCustomer = null;
        }
    }

    @Then("a customer named {string} should exist")
    public void a_customer_named_should_exist(String customerName) {
        assertNotNull(lastCreatedCustomer, "The customer should have been created");
        assertEquals(customerName, lastCreatedCustomer.getName());
        assertTrue(customerRepository.findByName(customerName).isPresent(), "Customer should be in the database");
    }

    @Then("the customer creation should fail")
    public void the_customer_creation_should_fail() {
        assertNull(lastCreatedCustomer, "Customer creation should have failed");
    }
} 