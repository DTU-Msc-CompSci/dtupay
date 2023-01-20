Feature: Payment
	Scenario: Successful Payment
		Given a customer with a bank account with balance 1000
		And a customer registered with DTU Pay
		And a token associated with the customer
		Given a merchant with a bank account with balance 2000
		And  a merchant registered with DTU Pay
		When the merchant initiates a payment for 100 kr with the customer token
		Then the transaction is successful
		And the balance of the customer at the bank is 900 kr
		And the balance of the merchant at the bank is 2100 kr
