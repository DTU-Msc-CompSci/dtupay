Feature: Token
  Scenario: Request token successful
    Given a customer registered with DTU Pay
    When the customer requests 4 tokens
    Then the customer receives 4 tokens

   Scenario: Request more tokens than available
     Given a customer registered with DTU Pay
     And the customer has already requested 5 unused tokens
     When the customer requests 4 tokens
     Then the token request fails and throws an exception "Not enough tokens available"

   Scenario: User requests more than 5 tokens
     Given a customer registered with DTU Pay
     When the customer requests 5 tokens
     Then the token request fails and throws and exception "More than 5 tokens requested"

   Scenario: User that does not exists requests token
     Given a customer that is not registered with DTU Pay
     When the customer requests 5 tokens
     Then the token request fails and throws an exception "User does not exist"

  Scenario: User requests less than 1 token
    Given a customer registered with DTU Pay
    When the customer requests -3 tokens
    Then the token request fails and throws an exception "Less than 1 token requested"

  Scenario: User requests tokens when they have more than 1
    Given a customer registered with DTU Pay
    And the customer has already requested 3 tokens
    Then when the customer requests 2 tokens
    Then the token requests fails and throws an exception "User already has more than 1 token"


