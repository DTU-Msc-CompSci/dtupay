Feature: Correlation ID

  Scenario: Correlation ID is set in the Event to the Queue
    Given existing customer with bank ID "example"
    When the customer registers for DTU Pay
    Then the event is sent to be processed with a correlation ID

  Scenario: Correlation ID is present in the CustomerAccountCreated Event
    Given existing customer with bank ID "example"
    When the customer registers for DTU Pay
    Then the CustomerAccountCreated event has a correlation ID
