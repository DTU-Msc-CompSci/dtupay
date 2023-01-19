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