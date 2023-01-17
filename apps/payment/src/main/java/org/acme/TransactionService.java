package org.acme;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankServiceService;
import messaging.Event;
import messaging.MessageQueue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionService {
    // For RabbitMQ stuffs
    MessageQueue queue;



    List<Transaction> transactions = new ArrayList<>();
    private BankService bankService;

    String customerBankId;
    String merchantBankId;

    BigDecimal amount;

//    public List<DTUPayUser> getCustomers() {
//        return customers;
//    }

//    public Optional<DTUPayUser> getCustomer(String uniqueId) {
//        return users.stream().filter( (user) -> user.getUniqueId().toString().equals(uniqueId)).findFirst();
//    }

    public TransactionService(MessageQueue q, BankService bankService) {
        this.queue = q;
        this.bankService = bankService;
        this.queue.addHandler("TransactionRequested", this::handleTransactionRequested);
        this.queue.addHandler("MerchantInfoProvided", this::handleMerchantInfoProvided);
        this.queue.addHandler("CustomerInfoProvided", this::handleCustomerInfoProvided);
    }

    public void handleMerchantInfoProvided(Event ev) {
        var t = ev.getArgument(0, String.class);
        merchantBankId = t;


        checkTransactionInfo(ev.getArgument(1, UUID.class));

    }
        // Generate random number to tie event to the request

    public void handleCustomerInfoProvided(Event ev) {
        var t = ev.getArgument(0, String.class);
        customerBankId = (t);
        // Generate random number to tie event to the request
        checkTransactionInfo(ev.getArgument(1, UUID.class));

    }

    public void handleTransactionRequested(Event ev) {
        var t = ev.getArgument(0, Transaction.class);
        addTransaction(t);
        // Generate random number to tie event to the request
        checkTransactionInfo(ev.getArgument(1, UUID.class));



    }

    public void initiateTransaction(String customer, String merchant, BigDecimal amount, UUID transactionId) {
        // Right now bankId == DTUPayID but this should change when we add registration service
        //TODO fetch the bank id from a registration service

       // var customerBankAccountID = transaction.getCustomerId();
       // var merchantBankAccountID = transaction.getMerchantId();
        try {
            bankService.transferMoneyFromTo(customer, merchant, amount, "DTU Pay transaction");
            Event transactionCompletedEvent = new Event("TransactionCompleted", new Object[] { transactionId });
            queue.publish(transactionCompletedEvent);
        }catch (BankServiceException_Exception e){
            // error event
        }
       //     transactions.add(transaction);

    }

    public void addTransaction(Transaction t) {
        //TODO Query external BankService

        //t.setTransactionId(generateUniqueId());
        amount = BigDecimal.valueOf(t.getAmount());
        transactions.add(t);
        System.out.println("DTU Pay User added to service");
    }


    private void checkTransactionInfo(UUID transactionId) {
        if (merchantBankId != null && customerBankId != null && amount != null) {
            // TODO: Migrate to an adapter later

            initiateTransaction(customerBankId, merchantBankId, amount, transactionId);
        }
    }




}