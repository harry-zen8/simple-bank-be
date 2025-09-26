package com.example.banking.service;

public class EmailNotificationService implements NotificationService {

    @Override
    public void sendNotification(String customerId, String message) {
        // In a real application, this would use JavaMail or a third-party service.
        System.out.println("---");
        System.out.println("Sending EMAIL to " + customerId);
        System.out.println("Message: " + message);
        System.out.println("---");
    }
} 