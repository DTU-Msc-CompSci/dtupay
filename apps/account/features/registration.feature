Feature: Payment

  Scenario: Successful Customer Registration
    Given There is a costumer with empty id
    When the customer is being registered
    Then The customer added correctly