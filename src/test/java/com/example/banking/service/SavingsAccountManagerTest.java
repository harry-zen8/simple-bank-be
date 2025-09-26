package com.example.banking.service;

import com.example.banking.model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SavingsAccountManagerTest {

    private SavingsAccountManager savingsAccountManager;
    private Account testAccount;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        savingsAccountManager = new SavingsAccountManager();
        
        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setBalance(new BigDecimal("1000"));
        testAccount.setAccountType("SAVINGS");
        
        // Capture System.out for testing console output
        System.setOut(new PrintStream(outputStream));
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void testPerformDeposit_success() {
        // ARRANGE
        BigDecimal depositAmount = new BigDecimal("500");
        BigDecimal expectedBalance = new BigDecimal("1500");

        // ACT
        savingsAccountManager.performDeposit(testAccount, depositAmount);

        // ASSERT
        assertEquals(expectedBalance, testAccount.getBalance());
        assertTrue(outputStream.toString().contains("Deposited 500 into Savings Account 1"));
    }

    @Test
    void testPerformDeposit_zeroAmount() {
        // ARRANGE
        BigDecimal depositAmount = BigDecimal.ZERO;
        BigDecimal expectedBalance = new BigDecimal("1000");

        // ACT
        savingsAccountManager.performDeposit(testAccount, depositAmount);

        // ASSERT
        assertEquals(expectedBalance, testAccount.getBalance());
        assertTrue(outputStream.toString().contains("Deposited 0 into Savings Account 1"));
    }

    @Test
    void testPerformWithdrawal_sufficientFunds() {
        // ARRANGE
        BigDecimal withdrawalAmount = new BigDecimal("300");
        BigDecimal expectedBalance = new BigDecimal("700");

        // ACT
        savingsAccountManager.performWithdrawal(testAccount, withdrawalAmount);

        // ASSERT
        assertEquals(expectedBalance, testAccount.getBalance());
        assertTrue(outputStream.toString().contains("Withdrew 300 from Savings Account 1"));
    }

    @Test
    void testPerformWithdrawal_insufficientFunds() {
        // ARRANGE
        BigDecimal withdrawalAmount = new BigDecimal("1500");
        BigDecimal expectedBalance = new BigDecimal("1000"); // Should remain unchanged

        // ACT
        savingsAccountManager.performWithdrawal(testAccount, withdrawalAmount);

        // ASSERT
        assertEquals(expectedBalance, testAccount.getBalance());
        assertTrue(outputStream.toString().contains("Insufficient funds for withdrawal from Savings Account 1"));
    }

    @Test
    void testPerformWithdrawal_exactBalance() {
        // ARRANGE
        BigDecimal withdrawalAmount = new BigDecimal("1000");
        BigDecimal expectedBalance = BigDecimal.ZERO;

        // ACT
        savingsAccountManager.performWithdrawal(testAccount, withdrawalAmount);

        // ASSERT
        assertEquals(expectedBalance, testAccount.getBalance());
        assertTrue(outputStream.toString().contains("Withdrew 1000 from Savings Account 1"));
    }

    @Test
    void testApplyInterest_success() {
        // ARRANGE
        BigDecimal expectedInterest = new BigDecimal("20.00"); // 1000 * 0.02
        BigDecimal expectedBalance = new BigDecimal("1020.00");

        // ACT
        savingsAccountManager.applyInterest(testAccount);

        // ASSERT
        assertEquals(expectedBalance, testAccount.getBalance());
        assertTrue(outputStream.toString().contains("Applied interest of 20.00 to Savings Account 1"));
    }

    @Test
    void testApplyInterest_zeroBalance() {
        // ARRANGE
        testAccount.setBalance(BigDecimal.ZERO);
        BigDecimal expectedBalance = BigDecimal.ZERO;

        // ACT
        savingsAccountManager.applyInterest(testAccount);

        // ASSERT
        assertEquals(0, expectedBalance.compareTo(testAccount.getBalance()));
        assertTrue(outputStream.toString().contains("Applied interest of 0"));
    }

    @Test
    void testApplyInterest_highBalance() {
        // ARRANGE
        testAccount.setBalance(new BigDecimal("10000"));
        BigDecimal expectedInterest = new BigDecimal("200.00"); // 10000 * 0.02
        BigDecimal expectedBalance = new BigDecimal("10200.00");

        // ACT
        savingsAccountManager.applyInterest(testAccount);

        // ASSERT
        assertEquals(expectedBalance, testAccount.getBalance());
        assertTrue(outputStream.toString().contains("Applied interest of 200.00 to Savings Account 1"));
    }

    @Test
    void testProcessOverdraft_throwsException() {
        // ARRANGE & ACT & ASSERT
        UnsupportedOperationException exception = assertThrows(
            UnsupportedOperationException.class,
            () -> savingsAccountManager.processOverdraft(testAccount)
        );

        assertEquals("Overdraft processing is not supported for savings accounts.", exception.getMessage());
    }

    @Test
    void testMultipleOperations_sequence() {
        // ARRANGE
        BigDecimal depositAmount = new BigDecimal("500");
        BigDecimal withdrawalAmount = new BigDecimal("200");
        BigDecimal expectedBalanceAfterDeposit = new BigDecimal("1500");
        BigDecimal expectedBalanceAfterWithdrawal = new BigDecimal("1300");
        BigDecimal expectedInterest = new BigDecimal("26.00"); // 1300 * 0.02
        BigDecimal expectedFinalBalance = new BigDecimal("1326.00");

        // ACT - Perform sequence of operations
        savingsAccountManager.performDeposit(testAccount, depositAmount);
        assertEquals(expectedBalanceAfterDeposit, testAccount.getBalance());

        savingsAccountManager.performWithdrawal(testAccount, withdrawalAmount);
        assertEquals(expectedBalanceAfterWithdrawal, testAccount.getBalance());

        savingsAccountManager.applyInterest(testAccount);
        assertEquals(expectedFinalBalance, testAccount.getBalance());

        // ASSERT - Verify all console outputs
        String output = outputStream.toString();
        assertTrue(output.contains("Deposited 500 into Savings Account 1"));
        assertTrue(output.contains("Withdrew 200 from Savings Account 1"));
        assertTrue(output.contains("Applied interest of 26.00 to Savings Account 1"));
    }

    @Test
    void testPerformDeposit_nullAmount_throwsException() {
        // ARRANGE & ACT & ASSERT
        assertThrows(NullPointerException.class, () -> {
            savingsAccountManager.performDeposit(testAccount, null);
        });
    }

    @Test
    void testPerformWithdrawal_nullAmount_throwsException() {
        // ARRANGE & ACT & ASSERT
        assertThrows(NullPointerException.class, () -> {
            savingsAccountManager.performWithdrawal(testAccount, null);
        });
    }

    @Test
    void testPerformDeposit_negativeAmount() {
        // ARRANGE
        BigDecimal negativeAmount = new BigDecimal("-100");
        BigDecimal expectedBalance = new BigDecimal("900"); // 1000 + (-100)

        // ACT
        savingsAccountManager.performDeposit(testAccount, negativeAmount);

        // ASSERT
        assertEquals(expectedBalance, testAccount.getBalance());
        assertTrue(outputStream.toString().contains("Deposited -100 into Savings Account 1"));
    }

    @Test
    void testPerformWithdrawal_negativeAmount() {
        // ARRANGE
        BigDecimal negativeAmount = new BigDecimal("-100");
        BigDecimal expectedBalance = new BigDecimal("1100"); // 1000 - (-100) = 1000 + 100

        // ACT
        savingsAccountManager.performWithdrawal(testAccount, negativeAmount);

        // ASSERT
        assertEquals(expectedBalance, testAccount.getBalance());
        assertTrue(outputStream.toString().contains("Withdrew -100 from Savings Account 1"));
    }
}