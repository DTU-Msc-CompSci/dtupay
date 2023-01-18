package org.acme;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankServiceService;
import messaging.Event;
import messaging.MessageQueue;
import org.acme.aggregate.Transaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionService {
    // For RabbitMQ stuffs
    MessageQueue queue;


    List<Transaction> transactions = new ArrayList<>();
    private BankService bankService = new BankServiceService().getBankServicePort();

    // I don't think these are thread-safe
    String customerBankId;
    String merchantBankId;

    BigDecimal amount;
    // This feels like a dirty hack
    String correlationId;


    public String generateUniqueId() {
        return UUID.randomUUID().toString();
    }

    public TransactionService(MessageQueue q) {
        this.queue = q;
        this.queue.addHandler("TransactionRequested", this::handleTransactionRequested);
        this.queue.addHandler("MerchantInfoProvided", this::handleMerchantInfoProvided);
        this.queue.addHandler("CustomerInfoProvided", this::handleCustomerInfoProvided);


    }

    public void handleMerchantInfoProvided(Event ev) {
        correlationId = ev.getArgument(0, String.class);
        var t = ev.getArgument(1, String.class);
        merchantBankId = t;


        checkTransactionInfo();

    }
    // Generate random number to tie event to the request

    public void handleCustomerInfoProvided(Event ev) {
        correlationId = ev.getArgument(0, String.class);
        var t = ev.getArgument(1, String.class);
        customerBankId = (t);
        // Generate random number to tie event to the request
        checkTransactionInfo();

    }

    public void handleTransactionRequested(Event ev) {
        correlationId = ev.getArgument(0, String.class);
        var t = ev.getArgument(1, Transaction.class);
        addTransaction(t);
        // Generate random number to tie event to the request
        checkTransactionInfo();
    }

    public void initiateTransaction(String customer, String merchant, BigDecimal amount) {
        // Right now bankId == DTUPayID but this should change when we add registration service
        //TODO fetch the bank id from a registration service

        // var customerBankAccountID = transaction.getCustomerId();
        // var merchantBankAccountID = transaction.getMerchantId();
        try {
            bankService.transferMoneyFromTo(customer, merchant, amount, "DTU Pay transaction");
            Event transactionCompletedEvent = new Event("TransactionCompleted", new Object[]{ correlationId, "completed"});
            queue.publish(transactionCompletedEvent);
        } catch (BankServiceException_Exception e) {
            // error event
        }
        //     transactions.add(transaction);

    }

    public void addTransaction(Transaction t) {
        //TODO Query external BankService

        t.setTransactionId(generateUniqueId());
        amount = BigDecimal.valueOf(t.getAmount());
        transactions.add(t);
    }


    private void checkTransactionInfo() {
        if (merchantBankId != null && customerBankId != null && amount != null) {
            // TODO: Migrate to an adapter later

            initiateTransaction(customerBankId, merchantBankId, amount);
        }
    }


}