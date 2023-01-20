Feature: Account Service

  Scenario: Customer registration successful
    Given a customer with the first name "First", last name "Last", and CPR number "123456-7890"
    And a bank account for the customer
    When the customer tries to register
    Then registration is successful for the customer

  Scenario: Merchant registration successful
    Given a merchant with the first name "First", last name "Last", and CPR number "123456-7890"
    And a bank account for the merchant
    When the merchant tries to register
    Then registration is successful for the merchant

#  Scenario: Customer de-register successfully
#    Given a customer exists in DTUPay
#    When the customer de-registers
#    Then the customer is removed from DTUPay
#
#
#  Scenario: Merchant de-register successfully
#    Given a merchant exists in DTUPay
#    When the merchant de-registers
#    Then the merchant is removed from DTUPay
#
#  Scenario: Customer de-register unsuccessfully
#    Given a customer does not exist in DTUPay
#    When the customer de-registers
#    Then the customer gets an error message