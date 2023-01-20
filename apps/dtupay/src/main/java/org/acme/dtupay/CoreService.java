package org.acme.dtupay;

import messaging.Event;
import messaging.MessageQueue;
import java.util.UUID;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CoreService {
    private MessageQueue queue;


    // TODO: Migrate these concurrent-safe collection
    private Map<String, CompletableFuture<AccountResponse>> pendingCustomers = new ConcurrentHashMap<>();
    private Map<String, CompletableFuture<AccountResponse>> pendingMerchants = new ConcurrentHashMap<>();

    // All the things that are pending from Core Service as Origin and expects some response
    private Map<String, CompletableFuture<Boolean>> pendingDeregisterCustomers = new ConcurrentHashMap<>();
    private Map<String, CompletableFuture<Boolean>> pendingDeregisterMerchants = new ConcurrentHashMap<>();
    private Map<String, CompletableFuture<TokenResponse>> pendingTokenRequests = new ConcurrentHashMap<>();
    private Map<String, CompletableFuture<String>> pendingTransactions = new ConcurrentHashMap<>();
//    private CompletableFuture<AccountResponse> registeredCustomer;
//    private CompletableFuture<AccountResponse> registeredMerchant;
    private boolean tokenRemoved;
//    private CompletableFuture<Boolean> deRegisteredCustomerCompleted;
//    private CompletableFuture<Boolean> deRegisteredMerchantCompleted;
//    private boolean deRegisteredCustomer;

//    private CompletableFuture<TokenResponse> requestedToken;
//    private CompletableFuture<String> requestedTransaction;

    long timeoutValue = 10;
    TimeUnit timeoutUnit = TimeUnit.SECONDS;
    public CoreService(MessageQueue q) {
        queue = q;
        queue.addHandler("CustomerAccountCreated", this::handleCustomerRegistered);
        queue.addHandler("MerchantAccountCreated", this::handleMerchantRegistered);

        queue.addHandler("TokenRequestFulfilled", this::handleRequestedToken);
        queue.addHandler("TokenRequestFailed", this::handleTokenRequestFailed);

        queue.addHandler("TransactionCompleted", this::handleTransactionCompleted);
        queue.addHandler("CustomerAccountDeRegistrationCompleted", this::handleCustomerDeRegistrationCompleted);
        queue.addHandler("MerchantAccountDeRegistrationCompleted", this::handleMerchantDeRegistrationCompleted);
        queue.addHandler("CustomerAccountDeRegistrationFailed", this::handleCustomerDeRegistrationCompleted);
        queue.addHandler("MerchantAccountDeRegistrationFailed", this::handleMerchantDeRegistrationCompleted);
        queue.addHandler("AllTokenRemovedFromDeRegisteredCustomer", this::handleAllTokenRemovedFromDeRegisteredCustomer);
    }

    public void handleAllTokenRemovedFromDeRegisteredCustomer(Event ev) {
        var correlationId = ev.getArgument(0, String.class);
        tokenRemoved = ev.getArgument(1, Boolean.class);
        if (tokenRemoved) {
            pendingDeregisterCustomers.get(correlationId).complete(true);
        } else {
            pendingDeregisterCustomers.get(correlationId).complete(false);
        }
//        if(deRegisteredCustomer) {
//            deRegisteredCustomerCompleted.complete(true);
//        }
    }

    Map<String, CompletableFuture<AccountResponse>> getPendingCustomers() {
        return pendingCustomers;
    }

    public Boolean deRegisterCustomer(DTUPayUser user) {
        var correlationId = generateCorrelationId();
        CompletableFuture<Boolean> deRegisteredCustomerCompleted = new CompletableFuture<>();
        Event event = new Event("CustomerAccountDeRegistrationRequested", new Object[] {correlationId, user.getUniqueId()});
        pendingDeregisterCustomers.put(correlationId, deRegisteredCustomerCompleted);
        queue.publish(event);
        return deRegisteredCustomerCompleted.join();
        //return "De-registration request sent";
    }

    public Boolean deRegisterMerchant(DTUPayUser user) {
        var correlationId = generateCorrelationId();
        CompletableFuture<Boolean> deRegisteredMerchantCompleted = new CompletableFuture<>();
        Event event = new Event("MerchantAccountDeRegistrationRequested", new Object[] {correlationId, user.getUniqueId()});
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
        Event event = new Event("CustomerAccountCreationRequested", new Object[] { correlationId, c });
        queue.publish(event);
        // Might need to migrate to .get() and throw and exception to the caller; might change test steps as a result
        return registeredCustomerFuture.join();
    }

    public AccountResponse registerMerchant(DTUPayUser c) {
        CompletableFuture<AccountResponse> registeredMerchantFuture = new CompletableFuture<>();
        registeredMerchantFuture.orTimeout(timeoutValue, timeoutUnit);
        var correlationId = generateCorrelationId();
        Event event = new Event("MerchantAccountCreationRequested", new Object[] { correlationId, c });
        pendingMerchants.put(correlationId, registeredMerchantFuture);
        queue.publish(event);
        return registeredMerchantFuture.join();
    }

    public void handleCustomerRegistered(Event e) {
        var correlationId = e.getArgument(0, String.class);
        var s = e.getArgument(1, AccountResponse.class);
        // TODO: I don't think this is returning anything, and probably should be
        completePendingCustomerFutureByCorrelationId(correlationId, s);
    }

    public void handleMerchantRegistered(Event e) {
        var correlationId = e.getArgument(0, String.class);
        var s = e.getArgument(1, AccountResponse.class);
        // TODO: I don't think this is returning anything, and probably should be
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
        requestedToken.orTimeout(timeoutValue, timeoutUnit);
        Event event = new Event("TokenRequested", new Object[] { correlationId, t });
        queue.publish(event);
        pendingTokenRequests.put(correlationId, requestedToken);
        return requestedToken.get();
    }

    public void handleRequestedToken(Event e) {
        var correlationId = e.getArgument(0, String.class);
        var s = e.getArgument(1, TokenResponse.class);
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
        Event event = new Event("TransactionRequested", new Object[] { correlationId, t });
        queue.publish(event);
        pendingTransactions.put(correlationId, requestedTransaction);
        return requestedTransaction.join();
    }

    public void handleTransactionCompleted(Event e) {
        var id = e.getArgument(0, String.class);
        var s = e.getArgument(1, String.class);
        completePendingTransactionFutureByCorrelationId(id, s);
        // TODO standardize the response
    }

    // Helper functions
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