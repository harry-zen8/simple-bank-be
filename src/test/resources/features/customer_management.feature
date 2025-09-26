Feature: Customer Management

  Scenario: A new customer is created successfully
    When a new customer is created with the name "John Doe"
    Then a customer named "John Doe" should exist

  Scenario: Attempting to create a customer with a name that already exists
    Given a customer "Jane Doe" already exists
    When a new customer is created with the name "Jane Doe"
    Then the customer creation should fail 