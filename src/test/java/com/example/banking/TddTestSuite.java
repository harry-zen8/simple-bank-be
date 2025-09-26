package com.example.banking;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * TDD Test Suite - Comprehensive test suite for TDD exercises and validation.
 * 
 * This suite runs all unit tests, integration tests, and BDD tests to ensure
 * comprehensive coverage of the Simple Bank application.
 * 
 * Usage:
 * - Run this suite to execute all tests: mvn test -Dtest=TddTestSuite
 * - Use for TDD validation and continuous integration
 * - Ensures all TDD exercises are properly implemented
 */
@Suite
@SuiteDisplayName("TDD Test Suite - Simple Bank Application")
@SelectPackages({
    "com.example.banking.service",
    "com.example.banking.controller",
    "com.example.banking.bdd"
})
public class TddTestSuite {
    
    // This class serves as a test suite configuration.
    // All tests in the specified packages will be executed when this suite runs.
    
    /**
     * Test Categories Included:
     * 
     * 1. Service Layer Tests:
     *    - AccountServiceTest: Core account operations
     *    - CustomerServiceTest: Customer management
     *    - FeeCalculationServiceTest: Fee calculation logic
     *    - EmailNotificationServiceTest: Notification system
     *    - SavingsAccountManagerTest: Savings account operations
     * 
     * 2. Controller Layer Tests:
     *    - AccountControllerTest: REST API endpoints
     * 
     * 3. BDD Tests:
     *    - Cucumber feature tests for business scenarios
     * 
     * 4. Integration Tests:
     *    - End-to-end workflow validation
     */
}
