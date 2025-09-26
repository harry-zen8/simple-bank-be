package com.example.banking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    private EmailNotificationService emailNotificationService;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        emailNotificationService = new EmailNotificationService();
        
        // Capture System.out for testing console output
        System.setOut(new PrintStream(outputStream));
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void testSendNotification_success() {
        // ARRANGE
        String customerId = "12345";
        String message = "Your account balance is low";

        // ACT
        emailNotificationService.sendNotification(customerId, message);

        // ASSERT
        String output = outputStream.toString();
        assertTrue(output.contains("Sending EMAIL to 12345"));
        assertTrue(output.contains("Message: Your account balance is low"));
        assertTrue(output.contains("---"));
    }

    @Test
    void testSendNotification_emptyMessage() {
        // ARRANGE
        String customerId = "67890";
        String message = "";

        // ACT
        emailNotificationService.sendNotification(customerId, message);

        // ASSERT
        String output = outputStream.toString();
        assertTrue(output.contains("Sending EMAIL to 67890"));
        assertTrue(output.contains("Message: "));
        assertTrue(output.contains("---"));
    }

    @Test
    void testSendNotification_nullCustomerId() {
        // ARRANGE
        String customerId = null;
        String message = "Test message";

        // ACT
        emailNotificationService.sendNotification(customerId, message);

        // ASSERT
        String output = outputStream.toString();
        assertTrue(output.contains("Sending EMAIL to null"));
        assertTrue(output.contains("Message: Test message"));
        assertTrue(output.contains("---"));
    }

    @Test
    void testSendNotification_nullMessage() {
        // ARRANGE
        String customerId = "11111";
        String message = null;

        // ACT
        emailNotificationService.sendNotification(customerId, message);

        // ASSERT
        String output = outputStream.toString();
        assertTrue(output.contains("Sending EMAIL to 11111"));
        assertTrue(output.contains("Message: null"));
        assertTrue(output.contains("---"));
    }

    @Test
    void testSendNotification_longMessage() {
        // ARRANGE
        String customerId = "99999";
        String message = "This is a very long message that contains multiple lines and special characters: !@#$%^&*()_+-=[]{}|;':\",./<>?";

        // ACT
        emailNotificationService.sendNotification(customerId, message);

        // ASSERT
        String output = outputStream.toString();
        assertTrue(output.contains("Sending EMAIL to 99999"));
        assertTrue(output.contains("Message: " + message));
        assertTrue(output.contains("---"));
    }

    @Test
    void testSendNotification_specialCharactersInCustomerId() {
        // ARRANGE
        String customerId = "CUST-123_ABC@domain.com";
        String message = "Account update notification";

        // ACT
        emailNotificationService.sendNotification(customerId, message);

        // ASSERT
        String output = outputStream.toString();
        assertTrue(output.contains("Sending EMAIL to CUST-123_ABC@domain.com"));
        assertTrue(output.contains("Message: Account update notification"));
        assertTrue(output.contains("---"));
    }

    @Test
    void testSendNotification_multipleCalls() {
        // ARRANGE
        String customerId1 = "11111";
        String message1 = "First notification";
        String customerId2 = "22222";
        String message2 = "Second notification";

        // ACT
        emailNotificationService.sendNotification(customerId1, message1);
        emailNotificationService.sendNotification(customerId2, message2);

        // ASSERT
        String output = outputStream.toString();
        assertTrue(output.contains("Sending EMAIL to 11111"));
        assertTrue(output.contains("Message: First notification"));
        assertTrue(output.contains("Sending EMAIL to 22222"));
        assertTrue(output.contains("Message: Second notification"));
        
        // Count occurrences of separator lines
        long separatorCount = output.chars().filter(ch -> ch == '-').count();
        assertEquals(12, separatorCount); // 6 separators per call (---\n---), 2 calls = 12 total
    }

    @Test
    void testSendNotification_whitespaceHandling() {
        // ARRANGE
        String customerId = "  12345  ";
        String message = "  Message with leading and trailing spaces  ";

        // ACT
        emailNotificationService.sendNotification(customerId, message);

        // ASSERT
        String output = outputStream.toString();
        assertTrue(output.contains("Sending EMAIL to   12345  "));
        assertTrue(output.contains("Message:   Message with leading and trailing spaces  "));
        assertTrue(output.contains("---"));
    }

    @Test
    void testSendNotification_unicodeCharacters() {
        // ARRANGE
        String customerId = "客户123";
        String message = "您的账户余额不足。请及时充值。";

        // ACT
        emailNotificationService.sendNotification(customerId, message);

        // ASSERT
        String output = outputStream.toString();
        assertTrue(output.contains("Sending EMAIL to 客户123"));
        assertTrue(output.contains("Message: 您的账户余额不足。请及时充值。"));
        assertTrue(output.contains("---"));
    }

    @Test
    void testSendNotification_implementsNotificationService() {
        // ARRANGE & ACT & ASSERT
        assertTrue(emailNotificationService instanceof NotificationService);
    }

    @Test
    void testSendNotification_outputFormat() {
        // ARRANGE
        String customerId = "TEST123";
        String message = "Test message";

        // ACT
        emailNotificationService.sendNotification(customerId, message);

        // ASSERT
        String output = outputStream.toString();
        String[] lines = output.split("\n");
        
        // Verify the exact format
        assertEquals("---", lines[0].trim());
        assertEquals("Sending EMAIL to TEST123", lines[1].trim());
        assertEquals("Message: Test message", lines[2].trim());
        assertEquals("---", lines[3].trim());
    }
}