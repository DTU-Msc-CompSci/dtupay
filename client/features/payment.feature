Feature: Payment
	#Cannot pass anymore as the payment service needs actual bank ids to successfully perform payment
#  Scenario: Basic Successful Payment
#    Given a customer with id "cid1"
#    And a merchant with id "mid1"
#    When the merchant initiates a payment for 10 kr by the customer
#    Then the payment is successful

#	# TODO: Implement later since more dependent on other services
#	Scenario: Successful Payment
#		Given a customer with a bank account with balance 1000
##		And that the customer is registered with DTU Pay
#		Given a merchant with a bank account with balance 2000
##		And that the merchant is registered with DTU Pay
#		When the merchant initiates a payment for 100 kr by the customer
#		Then the payment is successful
#		And the balance of the customer at the bank is 900 kr
#		And the balance of the merchant at the bank is 2100 kr

	Scenario: Successful Payment Baseline
		Given a customer registered with DTU Pay
#		Given a merchant registered with DTU Pay
#		When the merchant requests a transaction
#		And provides a customer's token
#		Then the transaction is successful


