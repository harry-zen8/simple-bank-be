package com.example.banking;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Complete Test Suite that runs all tests in the application
 * 
 * This suite includes:
 * - Unit Tests (Service layer with mocked dependencies)
 * - BDD Integration Tests (Cucumber with full Spring context)
 * - API Layer Integration Tests (@WebMvcTest)
 * - End-to-End API Tests (@SpringBootTest with TestRestTemplate)
 */
@Suite
@SuiteDisplayName("Complete Test Suite - All Tests")
@SelectPackages({
    "com.example.banking.service",           // Unit Tests
    "com.example.banking.bdd",               // BDD Integration Tests
    "com.example.banking.integration"        // All Integration Tests
})
public class AllTestsSuite {
    // This class serves as a complete test suite entry point
}
