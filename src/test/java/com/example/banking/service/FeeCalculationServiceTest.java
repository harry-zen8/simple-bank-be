package com.example.banking.service;

import com.example.banking.model.Account;
import com.example.banking.model.Customer;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.CustomerRepository;
import com.example.banking.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(MockitoExtension.class)
public class FeeCalculationServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private FeeCalculationService feeCalculationService;

    @Test
    void testHandle_bronzeCustomer() {
        Account account = new Account();
        account.setId(1L);
        account.setCustId(1L);
        account.setBalance(new BigDecimal("1000"));
        account.setAccountType("CHECKING");

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setCustomerLevel("BRONZE");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        feeCalculationService.handle(1L);

        assertEquals(new BigDecimal("990.00"), account.getBalance());
        verify(transactionRepository).save(any());
    }

    @Test
    void testHandle_silverCustomer() {
        Account account = new Account();
        account.setId(1L);
        account.setCustId(1L);
        account.setBalance(new BigDecimal("1000"));
        account.setAccountType("CHECKING");

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setCustomerLevel("SILVER");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        feeCalculationService.handle(1L);

        assertEquals(0, new BigDecimal("995.00").compareTo(account.getBalance())); // 50% of 10.00 fee
        verify(transactionRepository).save(any());
    }

    @Test
    void testHandle_goldCustomer() {
        Account account = new Account();
        account.setId(1L);
        account.setCustId(1L);
        account.setBalance(new BigDecimal("1000"));
        account.setAccountType("CHECKING");

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setCustomerLevel("GOLD");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        feeCalculationService.handle(1L);

        assertEquals(0, new BigDecimal("1000").compareTo(account.getBalance()));
        verify(transactionRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void testHandle_highBalanceSavings() {
        Account account = new Account();
        account.setId(1L);
        account.setCustId(1L);
        account.setBalance(new BigDecimal("6000"));
        account.setAccountType("SAVINGS");

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setCustomerLevel("BRONZE"); // Even bronze gets it waived

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        feeCalculationService.handle(1L);

        assertEquals(0, new BigDecimal("6000").compareTo(account.getBalance()));
        verify(transactionRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void testHandle_accountNotFound() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());
        feeCalculationService.handle(1L);
        // Verify no transactions or saves happened
        verify(transactionRepository, org.mockito.Mockito.never()).save(any());
        verify(accountRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void testHandle_customerNotFound() {
        Account account = new Account();
        account.setId(1L);
        account.setCustId(1L);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        feeCalculationService.handle(1L);

        // Verify no transactions or saves happened
        verify(transactionRepository, org.mockito.Mockito.never()).save(any());
        verify(accountRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void testHandle_lowBalanceSavings() {
        Account account = new Account();
        account.setId(1L);
        account.setCustId(1L);
        account.setBalance(new BigDecimal("1000"));
        account.setAccountType("SAVINGS");

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setCustomerLevel("BRONZE");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        feeCalculationService.handle(1L);

        // Fee should be applied for low-balance savings
        assertEquals(0, new BigDecimal("990.00").compareTo(account.getBalance()));
        verify(transactionRepository).save(any());
    }
} 