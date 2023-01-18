Feature: Payment

  Scenario: Successful Customer Registration
    Given There is a costumer with empty id
    When the customer is being registered
    Then The customer added correctly

  Scenario: Successful Customer Registration with Correlation ID
    Given There is a CustomerAccountCreationRequested event in the queue
    When the event is started
    Then the event is being processed and the correlation ID is present in the CustomerAccountCreationRequested event
