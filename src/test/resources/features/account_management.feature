Feature: Account Management

  Scenario: A new CHECKING account is created for an existing customer
    Given a customer "Savings Sally" already exists
    When a new "CHECKING" account is created for "Savings Sally"
    Then "Savings Sally" should have a "CHECKING" account

  Scenario: A customer makes a deposit into their account
    Given a customer "Depositor Dan" with a "CHECKING" account having a balance of 100.00
    When "Depositor Dan" deposits 50.00 into their "CHECKING" account
    Then the balance of "Depositor Dan"'s "CHECKING" account should be 150.00

  Scenario: A customer makes a withdrawal from their account
    Given a customer "Withdrawer Will" with a "CHECKING" account having a balance of 100.00
    When "Withdrawer Will" withdraws 30.00 from their "CHECKING" account
    Then the balance of "Withdrawer Will"'s "CHECKING" account should be 70.00

  Scenario: A customer attempts to withdraw more than their balance
    Given a customer "Overdraft Oscar" with a "CHECKING" account having a balance of 100.00
    When "Overdraft Oscar" withdraws 150.00 from their "CHECKING" account
    Then the withdrawal should fail
    And the balance of "Overdraft Oscar"'s "CHECKING" account should remain 100.00 