Feature: Payment
	Scenario: Successful Payment
		Given a customer with a bank account with balance 1000
		And a customer registered with DTU Pay
		And a token associated with the customer
		Given a merchant with a bank account with balance 2000
		And  a merchant registered with DTU Pay
		When the merchant initiates a payment for 100 kr with the customer token
		Then the transaction is successful

	Scenario: Not enough  money
		Given a customer with a bank account with balance 10
		And a customer registered with DTU Pay
		And a token associated with the customer
		Given a merchant with a bank account with balance 2000
		And a merchant registered with DTU Pay
		When the merchant initiates a payment for 100 kr with the customer token
		Then the transaction is unsuccessful
		And throws an exception "Transaction failed"
		And the balance of the customer at the bank is 10 kr
		And the balance of the merchant at the bank is 2000 kr

	Scenario: Invalid token

	Scenario: Merchant does not have an account



