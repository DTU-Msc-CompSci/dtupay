Feature: Reporting
	Scenario: Successful Reporting
		Given a successful transaction
		When the merchant, the customer and the manager ask for a report
		Then the transaction is in the report






