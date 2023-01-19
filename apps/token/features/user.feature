Feature: User management

  Scenario: Add user to token map
    Given a customer id "example1"
    When the service receives a TokenUserRequested event
    Then assignedTokens contains the customer id

  Scenario: Remove all tokens from user
    Given a customer id "example2"
    When the service receives a CustomerAccountDeRegistrationRequested event
    Then a AllTokenRemovedFromDeRegisteredCustomer event is published
    And assignedTokens does not contain the customer id
    And tokenToId does not contain the customer id as a value


#  Scenario: Transaction token to ID
#    Given a customer id "example3"
#    And the customer has 1 token with the id "dummyID"
#    When the service receives a TransactionRequested event
#    Then a TokenValidated event is published containing the customer id
#    And assignedTokens does not contain "dummyID"
#    And tokenToId does not contain "dummyID"
#    And usedTokenPool contains "dummyID"