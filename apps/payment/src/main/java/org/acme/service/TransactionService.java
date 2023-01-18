package org.acme.service;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankServiceService;
import messaging.Event;
import messaging.MessageQueue;
import org.acme.aggregate.DTUPayUser;
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

    public String generateUniqueId() {
        return UUID.randomUUID().toString();
    }
    private PaymentRepository repository;
    private ReadModelRepository readRepository;




    public TransactionService(MessageQueue q, PaymentRepository repository, ReadModelRepository readRepository) {
        this.queue = q;
        this.queue.addHandler("TransactionRequested", this::handlePayment);
        this.queue.addHandler("MerchantInfoProvided", this::handlePayment);
        this.queue.addHandler("CustomerInfoProvided", this::handlePayment);
        this.readRepository = readRepository;
        this.repository = repository;


    }


        // Generate random number to tie event to the request
        public void handlePayment(Event ev) {
            var id = ev.getArgument(0, String.class);

            handlePaymentForOnePayment(ev, id);

        }
    public synchronized void handlePaymentForOnePayment(Event ev, String id) {
        Payment payment = repository.getById(id);
        switch (ev.getType()){
            case "CustomerInfoProvided":
                var customerInfo = ev.getArgument(1, DTUPayUser.class);

                payment.addCustomerInfo(id,customerInfo);
                repository.save(payment);

                break;
            case "TransactionRequested":
                var transaction = ev.getArgument(1, Transaction.class);
                payment.create(id,transaction.getCustomerToken().getToken(),transaction.getMerchantId(),BigDecimal.valueOf(transaction.getAmount()));
                repository.save(payment);

                break;
            case "MerchantInfoProvided":
                var merchantInfo = ev.getArgument(1, DTUPayUser.class);

                payment.addMerchantInfo(id,merchantInfo);
                repository.save(payment);
                break;
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

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

    }



    private  void checkTransactionInfo(String id) {
        Payment payment = repository.getById(id);

        if (payment.complete()){
            initiateTransaction(payment.getCustomerBankID(), payment.getMerchantBankID(), payment.getAmount());

        }
    }




}