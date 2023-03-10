package org.acme.service;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import messaging.Event;
import messaging.MessageQueue;
import org.acme.aggregate.DTUPayUser;
import org.acme.aggregate.Payment;
import org.acme.aggregate.ReportManagerResponse;
import org.acme.aggregate.ReportUserResponse;
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
        var reports = readRepository.getAllPayments();
        ReportManagerResponse resp = new ReportManagerResponse();
        resp.setReports(reports);
        Event event2 = new Event("ManagerReportCreated", new Object[]{ id, resp});
        queue.publish(event2);

    }

    public void handleCustomerReportRequested(Event event) {

        var id = event.getArgument(0, String.class);
        var customerId = event.getArgument(1, String.class);

        var reports = readRepository.getCustomerPayment(customerId);
        ReportUserResponse resp = new ReportUserResponse();
        resp.setReports(reports);
        Event event2 = new Event("CustomerReportCreated", new Object[]{ id, resp});
        queue.publish(event2);

    }

    public void handleMerchantReportRequested(Event event) {
        var id = event.getArgument(0, String.class);
        var merchantId = event.getArgument(1, String.class);
        var reports = readRepository.getMerchantPayment(merchantId);
        ReportUserResponse resp = new ReportUserResponse();
        resp.setReports(reports);
        Event event2 = new Event("MerchantReportCreated", new Object[]{ id, resp});
        queue.publish(event2);

    }


    // Generate random number to tie event to the request
    public void handlePayment(Event ev) {
        var id = ev.getArgument(0, String.class);

        handlePaymentForOnePayment(ev, id);

    }

    public synchronized Void handlePaymentForOnePayment(Event ev, String id) {
        Payment payment = repository.getById(id);
        switch (ev.getType()) {
            case "CustomerInfoProvided":
                var customerInfo = ev.getArgument(1, DTUPayUser.class);
                if (customerInfo == null || customerInfo.equals("") || customerInfo.getUniqueId() == null) {
                    Event transactionFailedEvent = new Event("TransactionFailed", new Object[]{id, "Invalid token"});
                    queue.publish(transactionFailedEvent);
                    return null;
                }else{
                    payment.addCustomerInfo(id, customerInfo);
                    repository.save(payment);
                }



                break;
            case "TransactionRequested":
                var transaction = ev.getArgument(1, Transaction.class);
                if(transaction.getAmount() <= 1) {
                    Event transactionFailedEvent = new Event("TransactionFailed", new Object[]{id, "Must request a payment of at least 1 kr"});
                    queue.publish(transactionFailedEvent);
                    return null;
                }
                payment.create(id, transaction.getCustomerToken().getToken(), transaction.getMerchantId(), BigDecimal.valueOf(transaction.getAmount()));
                repository.save(payment);

                break;
            case "MerchantInfoProvided":
                var merchantInfo = ev.getArgument(1, DTUPayUser.class);
                if (merchantInfo == null || merchantInfo.equals("")) {
                    Event transactionFailedEvent = new Event("TransactionFailed", new Object[]{id, "Merchant does not exist"});
                    queue.publish(transactionFailedEvent);
                    return null;
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
        return null;

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
            initiateTransaction(payment.getCustomerBankID(), payment.getMerchantBankID(), payment.getAmount(), id);
        }
    }


}