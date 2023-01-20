package org.acme.service;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import messaging.Event;
import messaging.MessageQueue;
import org.acme.aggregate.DTUPayUser;
import org.acme.aggregate.Payment;
import org.acme.aggregate.Transaction;
import org.acme.repositories.PaymentRepository;
import org.acme.repositories.ReadModelRepository;

import java.math.BigDecimal;

public class TransactionService {
    MessageQueue queue;

    private final BankService bankService;

    private final PaymentRepository repository;
    private final ReadModelRepository readRepository;


    public TransactionService(MessageQueue q, BankService b, PaymentRepository repository, ReadModelRepository readRepository) {
        this.queue = q;
        this.queue.addHandler("TransactionRequested", this::handlePayment);
        this.queue.addHandler("MerchantInfoProvided", this::handlePayment);
        this.queue.addHandler("ManagerReportRequested", this::handleManagerReportRequested);
        this.queue.addHandler("CustomerReportRequested", this::handleCustomerReportRequested);
        this.queue.addHandler("MerchantReportRequested", this::handleMerchantReportRequested);
        this.queue.addHandler("CustomerInfoProvided", this::handlePayment);
        this.bankService = b;
        this.readRepository = readRepository;
        this.repository = repository;


    }

    public void handleManagerReportRequested(Event event) {
        var id = event.getArgument(0, String.class);
        var resp = readRepository.getAllPayments();
        Event event2 = new Event("ManagerReportCreated", new Object[]{id, resp});
        queue.publish(event2);

    }

    public void handleCustomerReportRequested(Event event) {

        var id = event.getArgument(0, String.class);
        var customerId = event.getArgument(1, String.class);

        var resp = readRepository.getCustomerPayment(customerId);
        Event event2 = new Event("CustomerReportCreated", new Object[]{id, resp});
        queue.publish(event2);

    }

    public void handleMerchantReportRequested(Event event) {
        var id = event.getArgument(0, String.class);
        var merchantId = event.getArgument(1, String.class);
        var resp = readRepository.getMerchantPayment(merchantId);
        Event event2 = new Event("MerchantReportCreated", new Object[]{id, resp});
        queue.publish(event2);

    }


    // Generate random number to tie event to the request
    public void handlePayment(Event ev) {
        var id = ev.getArgument(0, String.class);

        handlePaymentForOnePayment(ev, id);

    }

    public synchronized void handlePaymentForOnePayment(Event ev, String id) {
        Payment payment = repository.getById(id);
        switch (ev.getType()) {
            case "CustomerInfoProvided":
                var customerInfo = ev.getArgument(1, DTUPayUser.class);
                if (customerInfo == null || customerInfo.equals("")) {
                    Event transactionFailedEvent = new Event("TransactionFailed", new Object[]{id, "Invalid token"});
                    queue.publish(transactionFailedEvent);
                    return;
                }

                payment.addCustomerInfo(id, customerInfo);
                repository.save(payment);

                break;
            case "TransactionRequested":
                var transaction = ev.getArgument(1, Transaction.class);
                if(transaction.getAmount() <= 1) {
                    Event transactionFailedEvent = new Event("TransactionFailed", new Object[]{id, "Must request a payment of at least 1 kr"});
                    queue.publish(transactionFailedEvent);
                    return;
                }
                payment.create(id, transaction.getCustomerToken().getToken(), transaction.getMerchantId(), BigDecimal.valueOf(transaction.getAmount()));
                repository.save(payment);

                break;
            case "MerchantInfoProvided":
                var merchantInfo = ev.getArgument(1, DTUPayUser.class);
                if (merchantInfo == null || merchantInfo.equals("")) {
                    Event transactionFailedEvent = new Event("TransactionFailed", new Object[]{id, "Merchant does not exist"});
                    queue.publish(transactionFailedEvent);
                    return;
                }

                payment.addMerchantInfo(id, merchantInfo);
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


    public void initiateTransaction(String customer, String merchant, BigDecimal amount, String id) {
        try {
            bankService.transferMoneyFromTo(customer, merchant, amount, "DTU Pay transaction");
            Event transactionCompletedEvent = new Event("TransactionCompleted", new Object[]{id, "Success"});
            queue.publish(transactionCompletedEvent);
        } catch (BankServiceException_Exception e) {
            Event transactionFailedEvent = new Event("TransactionFailed", new Object[]{id, "Transaction failed"});
            queue.publish(transactionFailedEvent);
        }
    }


    private void checkTransactionInfo(String id) {
        Payment payment = repository.getById(id);

        if (payment.complete()) {
//            String customerID = payment.getCustomerID();
//            String merchantID = payment.getMerchantID();
//            BigDecimal amount = payment.getAmount();
//
//            System.out.println("CUSTOMER ID");
//            System.out.println(customerID);
//            System.out.println("MERCHANT ID");
//            System.out.println(merchantID);
//            System.out.println("AMOUNT");
//            System.out.println(amount);
//
//            if(customerID == null || customerID.isEmpty()) {
//                System.out.println("I AM HERE!!!!");
//                Event transactionFailedEvent = new Event("TransactionFailed", new Object[]{id, "Invalid token"});
//                queue.publish(transactionFailedEvent);
//            } else if(merchantID.equals("")) {
//                Event transactionFailedEvent = new Event("TransactionFailed", new Object[]{id, "Merchant does not exist"});
//                queue.publish(transactionFailedEvent);
//            }
            System.out.println("I SKIPPED IT!!!!!");
            initiateTransaction(payment.getCustomerBankID(), payment.getMerchantBankID(), payment.getAmount(), id);
        }
    }


}