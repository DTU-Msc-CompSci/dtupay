Feature: Transaction

  Scenario: Only one payment even if events happen concurrently
    Given a concurrent "CustomerInfoProvided" Event and a "MerchantInfoProvided" Even and a "TransactionRequested" Event
    When transaction is initiated
    Then money is only transferred once


  Scenario: Transaction information saved in the readonly repo
    Given "TransactionCustomerInfoAdded" Event
    When info is added to the view
    Then the repo contains customer information

  Scenario: Transaction fails at the bank
    When invalid transaction data is sent to the service
    Then a transactionFailed event is published

  Scenario: Request for merchant report
    When the merchant report is requested
    Then the correct report is sent to the merchant

  Scenario: Request for customer report
    When the customer report is requested
    Then the correct report is sent to the customer

  Scenario: Request for manager report
    When the manager report is requested
    Then the correct report is sent to the manager

  Scenario: Two transactions with interleaved events
    When the service receives events for two transactions interleaved
    Then the transferMoneyToFrom method of the bank is called twice with correct values
    And two transactionCompleted events are sent with correct values