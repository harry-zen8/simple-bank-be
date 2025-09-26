package com.example.banking.service;

import com.example.banking.model.Customer;
import com.example.banking.model.CustomerCreationRequest;
import com.example.banking.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public Customer createCustomer(CustomerCreationRequest request) {
        if (customerRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Customer with name " + request.getName() + " already exists.");
        }
        Customer newCustomer = new Customer(request.getName(), request.getEmail(), request.getPhone());
        return customerRepository.save(newCustomer);
    }

    public Customer getCustomer(long id) {
        return customerRepository.findById(id).get();
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> findByName(String name) {
        return customerRepository.findByName(name);
    }
} 