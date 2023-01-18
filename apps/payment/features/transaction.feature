         Feature: Transaction

           Scenario: Simple transaction
             When the service receives a TransactionRequested event
             And a CustomerInfoProvided event with the same payment id is received
             And a MerchantInfoProvided event with the same payment id is received
             Then the transferMoneyToFrom method of the bank is called
             And a transactionCompleted event is sent with the same payment id

           Scenario: Two transactions with interleaved events
             When the service receives events for two transactions interleaved
             Then the transferMoneyToFrom method of the bank is called twice with correct values
             And two transactionCompleted events are sent with correct values

           Scenario: Concurrent transaction
             When the service receives all three events for a transaction simultaneously
             Then the transferMoneyToFrom method of the bank is only called once
             And a transactionCompleted event is only sent once