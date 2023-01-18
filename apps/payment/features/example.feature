Feature: Payment

  Scenario: Successful Customer Registration
    Given A naive scenario


  Scenario: Only one payment even if events happen concurrently
    Given a concurrent "CustomerInfoProvided" Event and a "MerchantInfoProvided" Even and a "TransactionRequested" Event
    When transaction is initiated
    Then money is only transferred once