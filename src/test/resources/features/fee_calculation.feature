Feature: Fee Calculation for Customer Accounts

  Scenario: A GOLD customer is not charged any fees
    Given a customer "Goldie" with a "GOLD" status and an account having a balance of 1000.00
    When the system calculates the monthly maintenance fees
    Then the balance of "Goldie"'s account should remain 1000.00

  Scenario: A SILVER customer gets a 50% discount on fees
    Given a customer "Silver" with a "SILVER" status and an account having a balance of 1000.00
    When the system calculates the monthly maintenance fees
    Then the balance of "Silver"'s account should be 995.00

  Scenario: A BRONZE customer is charged the standard fee
    Given a customer "Bronze" with a "BRONZE" status and an account having a balance of 1000.00
    When the system calculates the monthly maintenance fees
    Then the balance of "Bronze"'s account should be 990.00

  Scenario: High-balance SAVINGS account fees are waived, regardless of customer level
    Given a customer "Saver" with a "BRONZE" status and a SAVINGS account having a balance of 6000.00
    When the system calculates the monthly maintenance fees
    Then the balance of "Saver"'s account should remain 6000.00 