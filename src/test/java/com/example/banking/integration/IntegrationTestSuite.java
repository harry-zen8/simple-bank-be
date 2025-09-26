package com.example.banking.integration;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Integration Test Suite that runs all integration tests
 * 
 * This suite includes:
 * - API Layer Integration Tests (@WebMvcTest)
 * - End-to-End API Tests (@SpringBootTest with TestRestTemplate)
 */
@Suite
@SuiteDisplayName("Integration Test Suite")
@SelectPackages({
    "com.example.banking.integration.api",
    "com.example.banking.integration.e2e"
})
public class IntegrationTestSuite {
    // This class serves as a test suite entry point
}
