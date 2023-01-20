Feature: Payment
	Scenario: Successful Payment
		Given a customer with a bank account with balance 1000
		And a customer registered with DTU Pay
		And a token associated with the customer
		Given a merchant with a bank account with balance 2000
		And  a merchant registered with DTU Pay
		When the merchant initiates a payment for 100 kr with the customer token
		Then the transaction is successful
#
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
		Given a merchant with a bank account with balance 2000
		And a merchant registered with DTU Pay
		And a customer token with the id "fakeToken"
		When the merchant initiates a payment for 100 kr with the customer token
		Then the transaction is unsuccessful
		And throws an exception "Invalid token"
		And the balance of the merchant at the bank is 2000 kr

	Scenario: Merchant does not have an account
		Given a customer with a bank account with balance 10
		And a customer registered with DTU Pay
		And a token associated with the customer
		Given a merchant id "fakeID"
		When the merchant initiates a payment for 100 kr with the customer token
		Then the transaction is unsuccessful
		And throws an exception "Merchant does not exist"

	Scenario: Payment amount is 0 or less
		Given a customer with a bank account with balance 1000
		And a customer registered with DTU Pay
		And a token associated with the customer
		Given a merchant with a bank account with balance 2000
		And a merchant registered with DTU Pay
		When the merchant initiates a payment for -50 kr with the customer token
		Then the transaction is unsuccessful
		And throws an exception "Must request a payment of at least 1 kr"
		And the balance of the customer at the bank is 1000 kr
		And the balance of the merchant at the bank is 2000 kr




