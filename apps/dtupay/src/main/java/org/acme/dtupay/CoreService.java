package org.acme.dtupay;

import messaging.Event;
import messaging.MessageQueue;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CoreService {
    private final MessageQueue queue;

    private final Map<String, CompletableFuture<AccountResponse>> pendingCustomers = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<AccountResponse>> pendingMerchants = new ConcurrentHashMap<>();

    private final Map<String, CompletableFuture<Boolean>> pendingDeregisterCustomers = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<Boolean>> pendingDeregisterMerchants = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<TokenResponse>> pendingTokenRequests = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<String>> pendingTransactions = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<ReportManagerResponse>> pendingManagerReport = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<ReportUserResponse>> pendingCustomerReport = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<ReportUserResponse>> pendingMerchantReport = new ConcurrentHashMap<>();

    long timeoutValue = 20;
    TimeUnit timeoutUnit = TimeUnit.SECONDS;

    public CoreService(MessageQueue q) {
        queue = q;
        queue.addHandler("CustomerAccountCreated", this::handleCustomerRegistered);
        queue.addHandler("MerchantAccountCreated", this::handleMerchantRegistered);

        queue.addHandler("TokenRequestFulfilled", this::handleRequestedToken);
        queue.addHandler("TokenRequestFailed", this::handleTokenRequestFailed);
        queue.addHandler("ManagerReportCreated", this::handleManagerReportCreated);
        queue.addHandler("CustomerReportCreated", this::handleCustomerReportCreated);
        queue.addHandler("MerchantReportCreated", this::handleMerchantReportCreated);

        queue.addHandler("TransactionCompleted", this::handleTransactionCompleted);
        queue.addHandler("TransactionFailed", this::handleTransactionCompleted);

        queue.addHandler("CustomerAccountDeRegistrationCompleted", this::handleCustomerDeRegistrationCompleted);
        queue.addHandler("MerchantAccountDeRegistrationCompleted", this::handleMerchantDeRegistrationCompleted);
        queue.addHandler("CustomerAccountDeRegistrationFailed", this::handleCustomerDeRegistrationCompleted);
        queue.addHandler("MerchantAccountDeRegistrationFailed", this::handleMerchantDeRegistrationCompleted);
        queue.addHandler("AllTokenRemovedFromDeRegisteredCustomer", this::handleAllTokenRemovedFromDeRegisteredCustomer);
    }

    public void handleManagerReportCreated(Event ev) {
        var correlationId = ev.getArgument(0, String.class);

        var report = ev.getArgument(1, ReportManagerResponse.class);
        pendingManagerReport.get(correlationId).complete(report);

    }
    public void handleCustomerReportCreated(Event ev) {
        var correlationId = ev.getArgument(0, String.class);

        var report = ev.getArgument(1, ReportUserResponse.class);
        pendingCustomerReport.get(correlationId).complete(report);

    }
    public void handleMerchantReportCreated(Event ev) {
        var correlationId = ev.getArgument(0, String.class);

        var report = ev.getArgument(1, ReportUserResponse.class);
        pendingMerchantReport.get(correlationId).complete(report);

    }
    public void handleAllTokenRemovedFromDeRegisteredCustomer(Event ev) {
        var correlationId = ev.getArgument(0, String.class);
        boolean tokenRemoved = ev.getArgument(1, Boolean.class);
        if (tokenRemoved) {
            pendingDeregisterCustomers.get(correlationId).complete(true);
        } else {
            pendingDeregisterCustomers.get(correlationId).complete(false);
        }
    }
    public ReportManagerResponse getManagerReports(){
        var correlationId = generateCorrelationId();
        Event event = new Event("ManagerReportRequested", new Object[] {correlationId});
        CompletableFuture<ReportManagerResponse> managerReport = new CompletableFuture<>();
        pendingManagerReport.put(correlationId, managerReport);

        queue.publish(event);

        return managerReport.join();

    }
    public ReportUserResponse getCustomerReports(String customerId){
        var correlationId = generateCorrelationId();
        Event event = new Event("CustomerReportRequested", new Object[] {correlationId,customerId });
        CompletableFuture<ReportUserResponse> customerReport = new CompletableFuture<>();
        pendingCustomerReport.put(correlationId, customerReport);

        queue.publish(event);

        return customerReport.join();

    }
    public ReportUserResponse getMerchantReports(String merchantId){
        var correlationId = generateCorrelationId();
        Event event = new Event("MerchantReportRequested", new Object[] {correlationId,merchantId });
        CompletableFuture<ReportUserResponse> merchantReport = new CompletableFuture<>();
        pendingMerchantReport.put(correlationId, merchantReport);

        queue.publish(event);

        return merchantReport.join();

    }

    Map<String, CompletableFuture<AccountResponse>> getPendingCustomers() {
        return pendingCustomers;
    }

    public Boolean deRegisterCustomer(DTUPayUser user) {
        var correlationId = generateCorrelationId();
        CompletableFuture<Boolean> deRegisteredCustomerCompleted = new CompletableFuture<>();
        Event event = new Event("CustomerAccountDeRegistrationRequested", new Object[]{correlationId, user.getUniqueId()});
        pendingDeregisterCustomers.put(correlationId, deRegisteredCustomerCompleted);
        queue.publish(event);
        return deRegisteredCustomerCompleted.join();
    }

    public Boolean deRegisterMerchant(DTUPayUser user) {
        var correlationId = generateCorrelationId();
        CompletableFuture<Boolean> deRegisteredMerchantCompleted = new CompletableFuture<>();
        Event event = new Event("MerchantAccountDeRegistrationRequested", new Object[]{correlationId, user.getUniqueId()});
        pendingDeregisterMerchants.put(correlationId, deRegisteredMerchantCompleted);
        queue.publish(event);
        return deRegisteredMerchantCompleted.join();
    }

    // TODO: All the events that are going to be generating the Correlation ID need to follow this pattern
    public AccountResponse registerCustomer(DTUPayUser c) {
        CompletableFuture<AccountResponse> registeredCustomerFuture = new CompletableFuture<>();
        registeredCustomerFuture.orTimeout(timeoutValue, timeoutUnit);
        var correlationId = generateCorrelationId();
        pendingCustomers.put(correlationId, registeredCustomerFuture);
        Event event = new Event("CustomerAccountCreationRequested", new Object[]{correlationId, c});
        queue.publish(event);
        return registeredCustomerFuture.join();
    }

    public AccountResponse registerMerchant(DTUPayUser c) {
        CompletableFuture<AccountResponse> registeredMerchantFuture = new CompletableFuture<>();
        registeredMerchantFuture.orTimeout(timeoutValue, timeoutUnit);
        var correlationId = generateCorrelationId();
        Event event = new Event("MerchantAccountCreationRequested", new Object[]{correlationId, c});
        pendingMerchants.put(correlationId, registeredMerchantFuture);
        queue.publish(event);
        return registeredMerchantFuture.join();
    }

    public void handleCustomerRegistered(Event e) {
        var correlationId = e.getArgument(0, String.class);
        var s = e.getArgument(1, AccountResponse.class);
        completePendingCustomerFutureByCorrelationId(correlationId, s);
    }

    public void handleMerchantRegistered(Event e) {
        var correlationId = e.getArgument(0, String.class);
        var s = e.getArgument(1, AccountResponse.class);
        completePendingMerchantFutureByCorrelationId(correlationId, s);
    }

    public void handleMerchantDeRegistrationCompleted(Event e) {
        var correlationId = e.getArgument(0, String.class);
        var s = e.getArgument(1, Boolean.class);
        pendingDeregisterMerchants.get(correlationId).complete(s);
        pendingDeregisterMerchants.remove(correlationId);
    }

    public void handleCustomerDeRegistrationCompleted(Event event) {
        var correlationId = event.getArgument(0, String.class);
        var deRegisteredCustomer = event.getArgument(1, Boolean.class);
        pendingDeregisterCustomers.get(correlationId).complete(deRegisteredCustomer);
        pendingDeregisterCustomers.remove(correlationId);
    }


    public TokenResponse getToken(TokenRequest t) throws Exception {
        var correlationId = generateCorrelationId();
        CompletableFuture<TokenResponse> requestedToken = new CompletableFuture<>();
        pendingTokenRequests.put(correlationId, requestedToken);
        //requestedToken.orTimeout(timeoutValue, timeoutUnit);
        Event event = new Event("TokenRequested", new Object[]{correlationId, t});
        queue.publish(event);
        return requestedToken.join(); // ??????
    }

    public void handleRequestedToken(Event e) {
        var correlationId = e.getArgument(0, String.class);
        var s = e.getArgument(1, TokenResponse.class);

        System.out.println("CORRELATION ID");
        System.out.println(correlationId);
        System.out.println("TOKEN RESPONSE");
        System.out.println(s);
        System.out.println("pendingTokenRequests");
        System.out.println(pendingTokenRequests.get(correlationId));

        pendingTokenRequests.get(correlationId).complete(s);
        pendingTokenRequests.remove(correlationId);
    }

    public void handleTokenRequestFailed(Event ev) {
        var correlationId = ev.getArgument(0, String.class);
        TokenResponse s = ev.getArgument(1, TokenResponse.class);


        pendingTokenRequests.get(correlationId).complete(s);
        pendingTokenRequests.remove(correlationId);
    }

    public String requestTransaction(Transaction t) {
        var correlationId = generateCorrelationId();
        var requestedTransaction = new CompletableFuture<String>();
        requestedTransaction.orTimeout(timeoutValue, timeoutUnit);
        Event event = new Event("TransactionRequested", new Object[]{correlationId, t});
        queue.publish(event);
        pendingTransactions.put(correlationId, requestedTransaction);
        return requestedTransaction.join();
    }

    public void handleTransactionCompleted(Event e) {
        var id = e.getArgument(0, String.class);
        var s = e.getArgument(1, String.class);
        completePendingTransactionFutureByCorrelationId(id, s);
    }

    public String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    public void completePendingCustomerFutureByCorrelationId(String correlationId, AccountResponse result) {
        var completeableFuture = pendingCustomers.get(correlationId);
        if (completeableFuture != null) {
            completeableFuture.complete(result);
            pendingCustomers.remove(correlationId);
        } else {
            throw new RuntimeException("No pending Customer future found for correlationId: " + correlationId);
        }
    }

    public void completePendingMerchantFutureByCorrelationId(String correlationId, AccountResponse result) {
        var completeableFuture = pendingMerchants.get(correlationId);
        if (completeableFuture != null) {
            completeableFuture.complete(result);
            pendingMerchants.remove(correlationId);
        } else {
            throw new RuntimeException("No pending Merchant future found for correlationId: " + correlationId);
        }
    }

    public void completePendingTransactionFutureByCorrelationId(String correlationId, String result) {
        var completeableFuture = pendingTransactions.get(correlationId);
        if (completeableFuture != null) {
            completeableFuture.complete(result);
            pendingTransactions.remove(correlationId);
        } else {
            throw new RuntimeException("No pending Transaction future found for correlationId: " + correlationId);
        }
    }

}
