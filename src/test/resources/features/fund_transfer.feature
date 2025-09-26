Feature: Fund Transfer
  As a user of the bank,
  I want to transfer money between two accounts,
  So that I can send money to other people.

  Scenario: A customer transfers funds to another customer's account successfully
    Given a customer "Alice" with an account having a balance of 100.00
    And a customer "Bob" with an account having a balance of 50.00
    When "Alice" transfers 20.00 to "Bob"
    Then the balance of "Alice"'s account should be 80.00
    And the balance of "Bob"'s account should be 70.00

  Scenario: A customer attempts to transfer more funds than they have
    Given a customer "Charlie" with an account having a balance of 40.00
    And a customer "David" with an account having a balance of 100.00
    When "Charlie" transfers 50.00 to "David"
    Then the transfer should fail
    And the balance of "Charlie"'s account should remain 40.00
    And the balance of "David"'s account should remain 100.00 