package org.acme;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import messaging.Event;
import messaging.MessageQueue;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionService {
    // For RabbitMQ stuffs
    MessageQueue queue;


    Map<String, TransactionData> transactions = new ConcurrentHashMap<>();
    private final BankService bankService;


    public TransactionService(MessageQueue q, BankService bankService) {
        this.queue = q;
        this.bankService = bankService;
        this.queue.addHandler("TransactionRequested", this::handleTransactionEvent);
        this.queue.addHandler("MerchantInfoProvided", this::handleTransactionEvent);
        this.queue.addHandler("CustomerInfoProvided", this::handleTransactionEvent);
    }
    // Sends a transfer money request to the bank
    // If the transfer is successful, a TransactionCompleted event is sent
    public void initiateTransaction(String customer, String merchant, BigDecimal amount, String correlationId) {

        try {
            bankService.transferMoneyFromTo(customer, merchant, amount, "DTU Pay transaction");
            Event transactionCompletedEvent = new Event("TransactionCompleted", new Object[] { correlationId });
            queue.publish(transactionCompletedEvent);
        }catch (BankServiceException_Exception e){
            //TODO: Handle exception
        }

    }
    // Same function for all events that are received here since they all do effectively the same thing
    // If the event received is the first of a transaction, a new TransactionData object is created
    // Then send event to a synchronized method that handles adding data to the TransactionData object
    public void handleTransactionEvent(Event ev) {
        var correlationId = ev.getArgument(0, String.class);
        transactions.putIfAbsent(correlationId, new TransactionData());
        addTransactionData(correlationId, ev);
    }
    // Add data to the transaction data object and check if it is complete, then initiate the transaction
    // This needs to be synchronized since multiple threads can access the same transaction data object
    public synchronized void addTransactionData(String correlationId, Event ev){
        TransactionData transactionData = transactions.get(correlationId);
        switch (ev.getType()){
            case "TransactionRequested":
                transactionData.setAmount(BigDecimal.valueOf(ev.getArgument(1, Transaction.class).getAmount()));
                break;
            case "CustomerInfoProvided":
                transactionData.setCustomerBankId(ev.getArgument(1, String.class));
                break;
            case "MerchantInfoProvided":
                transactionData.setMerchantBankId(ev.getArgument(1, String.class));
                break;
        }
        if(transactionData.isComplete()){
            initiateTransaction(transactionData.getCustomerBankId(), transactionData.getMerchantBankId(), transactionData.getAmount(), correlationId);
        }
    }




}