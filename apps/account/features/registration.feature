Feature: Account Manager

  Scenario: Successful Customer Registration
    Given There is a customer with empty id
    When the service receives a CustomerAccountCreationRequested event
    Then a CustomerAccountCreated event is published
    And the "customer" should exist in the database

  Scenario: Reject registration of an existing user
    Given There is a customer with empty id
    When the service receives a CustomerAccountCreationRequested event
    Then a CustomerAccountCreated event is published
    And the "customer" should exist in the database
    When the service receives a CustomerAccountCreationRequested event
    Then a CustomerAccountCreationFailed event is published because of "Duplicate User"

  Scenario: Reject registration of a customer with invalid bankId
    Given There is a "customer" with fake bankId
    When the service receives a CustomerAccountCreationRequested event
    Then a CustomerAccountCreationFailed event is published because of "Invalid BankAccountId"
    And the "customer" should not exist in the database

  Scenario: Successful Customer De-registration
    Given There is a customer with empty id
    When the service receives a CustomerAccountCreationRequested event
    Then a CustomerAccountCreated event is published
    And the "customer" should exist in the database
    When the service receives a CustomerAccountDeRegistrationRequested event
    Then a CustomerAccountDeRegistrationCompleted event is published
    And the "customer" should not exist in the database

  #Same tests for the merchant
  Scenario: Successful Merchant Registration
    Given There is a merchant with empty id
    When the service receives a MerchantAccountCreationRequested event
    Then a MerchantAccountCreated event is published
    And the "merchant" should exist in the database

  Scenario: Re-register an existing merchant
    Given There is a merchant with empty id
    When the service receives a MerchantAccountCreationRequested event
    Then a MerchantAccountCreated event is published
    And the "merchant" should exist in the database
    When the service receives a MerchantAccountCreationRequested event
    Then a MerchantAccountCreationFailed event is published because of "Duplicate User"

  Scenario: Reject registration of a merchant with invalid bankId
    Given There is a "merchant" with fake bankId
    When the service receives a MerchantAccountCreationRequested event
    Then a MerchantAccountCreationFailed event is published because of "Invalid BankAccountId"
    And the "merchant" should not exist in the database

  Scenario: Successful Merchant De-registration
    Given There is a merchant with empty id
    When the service receives a MerchantAccountCreationRequested event
    Then a MerchantAccountCreated event is published
    And the "merchant" should exist in the database
    When the service receives a MerchantAccountDeRegistrationRequested event
    Then a MerchantAccountDeRegistrationCompleted event is published
    And the "merchant" should not exist in the database

  #Transaction test
  Scenario: Normal transaction scenario.Since we test the registration
            extensively in the previous scenarios, we will skip them and
            focus on the transaction related events.
    Given There is a customer with empty id
    When the service receives a CustomerAccountCreationRequested event
    #Then a CustomerAccountCreated event is published
    #And the "customer" should exist in the database
    Given There is a merchant with empty id
    When the service receives a MerchantAccountCreationRequested event
    #Then a MerchantAccountCreated event is published
    #And the "merchant" should exist in the database
    When the service receives a TransactionRequested event
    Then a MerchantInfoProvided event is published
    When the service receives a TokenValidated event
    Then a CustomerInfoProvided event is published