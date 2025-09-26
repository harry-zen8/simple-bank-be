# Testing Guide for Simple Bank Application

This document provides comprehensive instructions for running and understanding the different types of tests in the Simple Bank application.

## Test Structure Overview

The application includes multiple layers of testing to ensure comprehensive coverage:

```
src/test/java/com/example/banking/
├── service/                          # Unit Tests
│   ├── AccountServiceTest.java
│   ├── CustomerServiceTest.java
│   ├── EmailNotificationServiceTest.java
│   ├── FeeCalculationServiceTest.java
│   └── SavingsAccountManagerTest.java
├── bdd/                              # BDD Integration Tests
│   ├── CucumberTestRunner.java
│   ├── CucumberSpringConfiguration.java
│   ├── TestContext.java
│   └── stepdefinitions/
├── integration/                      # Integration Tests
│   ├── api/                          # API Layer Tests
│   │   ├── AccountControllerIntegrationTest.java
│   │   └── CustomerControllerIntegrationTest.java
│   └── e2e/                          # End-to-End Tests
│       ├── AccountE2ETest.java
│       └── TransactionE2ETest.java
├── testconfig/                       # Test Configuration
│   ├── TestDatabaseConfig.java
│   └── TestSecurityConfig.java
├── IntegrationTestSuite.java         # Integration Test Runner
└── AllTestsSuite.java               # Complete Test Runner
```

## Test Types

### 1. Unit Tests
- **Location**: `src/test/java/com/example/banking/service/`
- **Technology**: JUnit 5 + Mockito
- **Scope**: Service layer with mocked dependencies
- **Purpose**: Test business logic in isolation

### 2. BDD Integration Tests
- **Location**: `src/test/java/com/example/banking/bdd/`
- **Technology**: Cucumber + Spring Boot Test
- **Scope**: Full application context with real services
- **Purpose**: Test business scenarios from user perspective

### 3. API Layer Integration Tests
- **Location**: `src/test/java/com/example/banking/integration/api/`
- **Technology**: `@WebMvcTest` + MockMvc
- **Scope**: HTTP endpoints with mocked service layer
- **Purpose**: Test API contracts and HTTP request/response handling

### 4. End-to-End API Tests
- **Location**: `src/test/java/com/example/banking/integration/e2e/`
- **Technology**: `@SpringBootTest` + TestRestTemplate
- **Scope**: Complete HTTP request/response cycle with real database
- **Purpose**: Test complete user workflows

## Running Tests

### Prerequisites
- Java 17+
- Maven 3.6+
- H2 Database (for testing)

### Running All Tests
```bash
# Run all tests
mvn test

# Run with test profile
mvn test -Dspring.profiles.active=test

# Run with verbose output
mvn test -X
```

### Running Specific Test Types

#### Unit Tests Only
```bash
mvn test -Dtest="*ServiceTest"
```

#### BDD Tests Only
```bash
mvn test -Dtest=CucumberTestRunner
```

#### API Integration Tests Only
```bash
mvn test -Dtest="*ControllerIntegrationTest"
```

#### End-to-End Tests Only
```bash
mvn test -Dtest="*E2ETest"
```

#### Integration Tests Only
```bash
mvn test -Dtest=IntegrationTestSuite
```

### Running Individual Test Classes
```bash
# Run specific test class
mvn test -Dtest=AccountServiceTest

# Run specific test method
mvn test -Dtest=AccountServiceTest#testCreateAccount
```

### Running Tests with Different Profiles
```bash
# Run with test profile (uses H2 in-memory database)
mvn test -Dspring.profiles.active=test

# Run with specific test database
mvn test -Dspring.datasource.url=jdbc:h2:mem:testdb
```

## Test Configuration

### Database Configuration
Tests use H2 in-memory database by default:
```properties
# application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.liquibase.enabled=true
spring.liquibase.contexts=test
```

### Security Configuration
Tests use a simplified security configuration that permits all requests:
```java
@TestConfiguration
public class TestSecurityConfig {
    @Bean
    @Primary
    @Profile("test")
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }
}
```

## Test Reports

### Code Coverage Report
```bash
# Generate coverage report
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Cucumber Reports
```bash
# Generate Cucumber reports
mvn test -Dtest=CucumberTestRunner

# View Cucumber reports
open target/cucumber-reports/index.html
```

### Surefire Test Reports
```bash
# View test results
open target/surefire-reports/index.html
```

## Test Data Management

### Test Isolation
- Each test class uses `@Transactional` for automatic rollback
- `@BeforeEach` methods clean up test data
- Tests are independent and can run in any order

### Test Data Builders
Use helper methods to create test data:
```java
private Customer createTestCustomer() {
    CustomerCreationRequest request = new CustomerCreationRequest();
    request.setName("Test Customer");
    request.setEmail("test@example.com");
    // ... set other fields
    return customerService.createCustomer(request);
}
```

## Best Practices

### 1. Test Naming
- Use descriptive test method names: `shouldCreateAccountSuccessfully()`
- Follow Given-When-Then pattern in test structure
- Use `@DisplayName` for complex test scenarios

### 2. Test Organization
- Group related tests in the same class
- Use `@Nested` classes for complex test scenarios
- Keep tests focused on single responsibility

### 3. Assertions
- Use AssertJ for fluent assertions: `assertThat(result).isEqualTo(expected)`
- Verify both positive and negative scenarios
- Check all relevant properties of returned objects

### 4. Mocking
- Mock external dependencies in unit tests
- Use real implementations in integration tests
- Verify mock interactions when necessary

### 5. Test Data
- Use realistic test data
- Test edge cases (null values, empty strings, large numbers)
- Clean up test data after each test

## Troubleshooting

### Common Issues

#### Tests Failing Due to Database Issues
```bash
# Clean and rebuild
mvn clean test

# Check database configuration
mvn test -Dspring.datasource.url=jdbc:h2:mem:testdb
```

#### Port Conflicts in E2E Tests
```bash
# Use random port
mvn test -Dspring.boot.test.web.environment=RANDOM_PORT
```

#### Memory Issues with Large Test Suites
```bash
# Increase memory for Maven
export MAVEN_OPTS="-Xmx2048m -XX:MaxPermSize=512m"
mvn test
```

#### Slow Test Execution
```bash
# Run tests in parallel
mvn test -Dparallel=methods -DthreadCount=4
```

### Debug Mode
```bash
# Run tests in debug mode
mvn test -Dmaven.surefire.debug

# Attach debugger to port 5005
```
