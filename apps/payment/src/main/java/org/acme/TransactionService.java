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
    private BankService bankService =  new BankServiceService().getBankServicePort();

    String customerBankId;
    String merchantBankId;

    BigDecimal amount;

//    public List<DTUPayUser> getCustomers() {
//        return customers;
//    }

//    public Optional<DTUPayUser> getCustomer(String uniqueId) {
//        return users.stream().filter( (user) -> user.getUniqueId().toString().equals(uniqueId)).findFirst();
//    }



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
        var t = ev.getArgument(0, String.class);
        merchantBankId = t;


        if (customerBankId != null) {
            // TODO: Migrate to an adapter later

            initiateTransaction(customerBankId, merchantBankId, amount);
        }
    }
        // Generate random number to tie event to the request

    public void handleCustomerInfoProvided(Event ev) {
        var t = ev.getArgument(0, String.class);
        customerBankId = (t);
        // Generate random number to tie event to the request
        if (merchantBankId != null) {
            // TODO: Migrate to an adapter later

            initiateTransaction(customerBankId, merchantBankId, amount);
        }
    }

    public void handleTransactionRequested(Event ev) {
        var t = ev.getArgument(0, Transaction.class);
        addTransaction(t);
        // Generate random number to tie event to the request
        Event merchantInfoEvent = new Event("MerchantInfoRequested", new Object[] { t.getMerchantId() });
        Event customerInfoEvent = new Event("InvalidateTokenRequested", new Object[] { t.getCustomerToken() });

        // This needs to respond to a different queue; which are interested in the "MerchantAccountCreated" topics
        // This is the "hat" that it wears
        queue.publish(merchantInfoEvent);
        queue.publish(customerInfoEvent);

    }

    public void initiateTransaction(String customer, String merchant, BigDecimal amount)  {
        // Right now bankId == DTUPayID but this should change when we add registration service
        //TODO fetch the bank id from a registration service

       // var customerBankAccountID = transaction.getCustomerId();
       // var merchantBankAccountID = transaction.getMerchantId();
        try {
            bankService.transferMoneyFromTo(customer, merchant, amount, "DTU Pay transaction");
            Event transactionCompletedEvent = new Event("TransactionCompleted", new Object[] { "completed" });
            queue.publish(transactionCompletedEvent);
        }catch (BankServiceException_Exception e){
            // error event
        }
       //     transactions.add(transaction);

    }

    public void addTransaction(Transaction t) {
        //TODO Query external BankService

        t.setTransactionId(generateUniqueId());
        amount = BigDecimal.valueOf(t.getAmount());
        transactions.add(t);
        System.out.println("DTU Pay User added to service");
    }




}