Feature: Account Manager

  Scenario: Successful Customer Registration
    Given There is a costumer with empty id
    When the service receives a CustomerAccountCreationRequested event
    Then a CustomerAccountCreated event is published

  Scenario: Re-register an existing customer
    Given There is a costumer with empty id
    When the service receives a CustomerAccountCreationRequested event
    Then a CustomerAccountCreated event is published
    When the service receives a CustomerAccountCreationRequested event
    Then a CustomerAccountFailed event is published

  Scenario: Successful Customer De-registration
    Given There is a costumer with empty id
    When the service receives a CustomerAccountCreationRequested event
    Then a CustomerAccountCreated event is published
    When the service receives a CustomerAccountDeRegistrationRequested event
    Then a CustomerAccountDeRegistrationCompleted event is published

  #Same tests for merchant
  Scenario: Successful Merchant Registration
    Given There is a merchant with empty id
    When the service receives a MerchantAccountCreationRequested event
    Then a MerchantAccountCreated event is published

  Scenario: Re-register an existing merchant
    Given There is a merchant with empty id
    When the service receives a MerchantAccountCreationRequested event
    Then a MerchantAccountCreated event is published
    When the service receives a MerchantAccountCreationRequested event
    Then a MerchantAccountCreationFailed event is published

  Scenario: Successful Merchant De-registration
    Given There is a merchant with empty id
    When the service receives a MerchantAccountCreationRequested event
    Then a MerchantAccountCreated event is published
    When the service receives a MerchantAccountDeRegistrationRequested event
    Then a MerchantAccountDeRegistrationCompleted event is published

