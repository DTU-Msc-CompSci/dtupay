Feature: Account Manager

  Scenario: Successful Customer Registration
    Given There is a costumer with empty id
    When the service receives a CustomerAccountCreationRequested event
    Then a CustomerAccountCreated event is published

#  Scenario: Try to register an existing costumer
#
#
#  Scenario: Successful Removal of Customer
#
#
#  Scenario: Successful Merchant Registration
#    Given There is a merchant with empty id
#    When the merchant is being registered
#    Then The merchant added correctly
#
#  Scenario: Try to register an existing merchant
#
#
#  Scenario: Successful Removal of Merchant