package org.acme;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankServiceService;
import jdk.jfr.Experimental;
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
            initiateTransaction(customerBankId, merchantBankId, amount);
        }
    }
        // Generate random number to tie event to the request

    public void handleCustomerInfoProvided(Event ev) {
        var t = ev.getArgument(0, String.class);
        customerBankId = (t);
        // Generate random number to tie event to the request
        if (merchantBankId != null) {
            initiateTransaction(customerBankId, merchantBankId, amount);
        }
    }

    public void handleTransactionRequested(Event ev) {
        var t = ev.getArgument(0, Transaction.class);
        addTransaction(t);
        // Generate random number to tie event to the request
        Event merchantInfoEvent = new Event("MerchantInfoRequested", new Object[] { t.merchantId });
        Event customerInfoEvent = new Event("CustomerInfoRequested", new Object[] { t.customerId });

        // This needs to respond to a different queue; which are interested in the "MerchantAccountCreated" topics
        // This is the "hat" that it wears
        queue.publish(merchantInfoEvent);
        queue.publish(customerInfoEvent);
        // TODO: Migrate to an adapter later
//        try {
//        initiateTransaction(t);
//        System.out.println("transaction completed");
//
//        }
//        catch (BankServiceException_Exception e){
//
//            Event event = new Event("MerchantAccountCreated", new Object[] { s });
//
//            // This needs to respond to a different queue; which are interested in the "MerchantAccountCreated" topics
//            // This is the "hat" that it wears
//            queue.publish(event);
//        }
//
//        // TODO: REMOVE ME
//        System.out.println("transaction completed");
    }

    public void initiateTransaction(String customer, String merchant, BigDecimal amount)  {
        // Right now bankId == DTUPayID but this should change when we add registration service
        //TODO fetch the bank id from a registration service
        var customerBankAccountID = transaction.getCid();
        var merchantBankAccountID = transaction.getMid();
        try {
            bankService.transferMoneyFromTo(customerBankAccountID,merchantBankAccountID,  BigDecimal.valueOf((transaction.getAmount())), "Empty description returns an error!! So, this description is fine for now");
            transactions.add(transaction);

        } catch (BankServiceException_Exception e) {
            throw new RuntimeException(e);
        }


    }

    public void addTransaction(Transaction t) {
        //TODO Query external BankService

        t.setTransactionId(generateUniqueId());
        amount = BigDecimal.valueOf(t.getAmount());
        transactions.add(t);
        System.out.println("DTU Pay User added to service");
    }




}