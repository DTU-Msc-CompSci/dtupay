Feature: Token Manager

 Scenario: Successful Token Generation
   Given a customer id "example"
   And the id is in the token map
   And a token request of 4
   When the service receives a TokenRequested event
   Then a TokenRequestFulfilled event is published
   And 4 tokens should exist for the user

 Scenario: User does not exist
   Given a customer id "example2"
   And a token request of 4
   When the service receives a TokenRequested event
   Then a TokenRequestFailed event is published saying "User does not exist"
   And 0 tokens should exist for the user

 Scenario: More than 5 tokens in request
   Given a customer id "example3"
   And the id is in the token map
   And a token request of 6
   When the service receives a TokenRequested event
   Then a TokenRequestFailed event is published saying "More than 5 tokens requested"
   And 0 tokens should exist for the user

 Scenario: User requests tokens when they have more than 1
   Given a customer id "example3"
   And the id is in the token map
   And the customer has 3 tokens
   And a token request of 2
   When the service receives a TokenRequested event
   Then a TokenRequestFailed event is published saying "User already has more than 1 token"
   And 3 tokens should exist for the user

 Scenario: User requests less than 1 token
   Given a customer id "example4"
   And the id is in the token map
   And a token request of -1
   When the service receives a TokenRequested event
   Then a TokenRequestFailed event is published saying "Less than 1 token requested"
   And 0 tokens should exist for the user