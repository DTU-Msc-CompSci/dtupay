package org.acme.service;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankServiceService;
import messaging.Event;
import messaging.MessageQueue;
import org.acme.aggregate.Payment;
import org.acme.aggregate.Transaction;
import org.acme.repositories.PaymentRepository;
import org.acme.repositories.ReadModelRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionService {
    // For RabbitMQ stuffs
    MessageQueue queue;

//    List<Transaction> transactions = new ArrayList<>();
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
    private PaymentRepository repository;
    private ReadModelRepository readRepository;




    public TransactionService(MessageQueue q, PaymentRepository repository, ReadModelRepository readRepository) {
        this.queue = q;
        this.queue.addHandler("TransactionRequested", this::handleTransactionRequested);
        this.queue.addHandler("MerchantInfoProvided", this::handleMerchantInfoProvided);
        this.queue.addHandler("CustomerInfoProvided", this::handleCustomerInfoProvided);
        this.readRepository = readRepository;
        this.repository = repository;


    }

    public void handleMerchantInfoProvided(Event ev) {
        var id = ev.getArgument(0, String.class);

        var t = ev.getArgument(1, String.class);
        Payment payment = repository.getById(id);

        payment.addMerchantBankID(t);
        // Generate random number to tie event to the request
        checkTransactionInfo(id);

    }
        // Generate random number to tie event to the request

    public void handleCustomerInfoProvided(Event ev) {
        var id = ev.getArgument(0, String.class);

        var t = ev.getArgument(1, String.class);
        Payment payment = repository.getById(id);

        payment.addCustomerBankID(t);
        // Generate random number to tie event to the request
        checkTransactionInfo(id);

    }

    public void handleTransactionRequested(Event ev) {
        var id = ev.getArgument(0, String.class);

        var t = ev.getArgument(1, Transaction.class);
        Payment payment = repository.getById(id);
        payment.create(id,t.getCustomerToken().getToken(),t.getMerchantId(),BigDecimal.valueOf(t.getAmount()));
        repository.save(payment);
        // Generate random number to tie event to the request
        checkTransactionInfo(id);



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

//    public void addTransaction(Transaction t) {
//        //TODO Query external BankService
//
//        t.setTransactionId(generateUniqueId());
//        amount = BigDecimal.valueOf(t.getAmount());
//        transactions.add(t);
//        System.out.println("DTU Pay User added to service");
//    }


    private synchronized void checkTransactionInfo(String transactionId) {
        var payment = repository.getById(transactionId);
        if (payment.complete()){
            initiateTransaction(payment.getCustomerBankID(), payment.getMerchantBankID(), payment.getAmount());

        }
    }




}