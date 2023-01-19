Feature: Token Manager

  Scenario: Successful Token Generation
    Given a customer id "example"
    And the id is in the token map
    And a token request of 4
    When the service receives a TokenRequested event
    Then a TokenRequestFulfilled event is published
    And 4 tokens should exist for the user

#  Scenario: User does not exist
#    Given a customer id
#    And a token request of 4
#    When the service receives a TokenRequested event
#    When a TokenRequestFailed event is published
#    And 0 tokens should exist for the user