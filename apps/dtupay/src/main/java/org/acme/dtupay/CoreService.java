package org.acme.dtupay;

import messaging.Event;
import messaging.MessageQueue;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class CoreService {
    private MessageQueue queue;


    // TODO: Migrate these concurrent-safe collection
    private Map<String, CompletableFuture<DTUPayUser>> pendingCustomers = new ConcurrentHashMap<>();
    private CompletableFuture<DTUPayUser> registeredCustomer;
    private CompletableFuture<DTUPayUser> registeredMerchant;

    private CompletableFuture<Token> requestedToken;
    private CompletableFuture<String> requestedTransaction;

    public CoreService(MessageQueue q) {
        queue = q;
        queue.addHandler("CustomerAccountCreated", this::handleCustomerRegistered);
        queue.addHandler("MerchantAccountCreated", this::handleMerchantRegistered);

        queue.addHandler("TokenRequestFulfilled", this::handleRequestedToken);
        queue.addHandler("TransactionCompleted", this::handleTransactionCompleted);
    }

    public Map<String, CompletableFuture<DTUPayUser>> getPendingCustomers() {
        return pendingCustomers;
    }

    // TODO: All the events that are going to be generating the Correlation ID need to follow this pattern
    public DTUPayUser registerCustomer(DTUPayUser c) {
        registeredCustomer = new CompletableFuture<>();
        var correlationId = generateCorrelationId();
        pendingCustomers.put(correlationId, registeredCustomer);
        Event event = new Event("CustomerAccountCreationRequested", new Object[] { correlationId, c });
        queue.publish(event);
        return registeredCustomer.join();
    }

    public DTUPayUser registerMerchant(DTUPayUser c) {
        registeredMerchant = new CompletableFuture<>();
        var correlationId = UUID.randomUUID().toString();
        Event event = new Event("MerchantAccountCreationRequested", new Object[] { correlationId, c });
        queue.publish(event);
        return registeredMerchant.join();
    }

    public void handleCustomerRegistered(Event e) {
        var correlationId = e.getArgument(0, String.class);
        var s = e.getArgument(1, DTUPayUser.class);
        completeFutureByCorrelationId(correlationId, s);
    }
    public void handleMerchantRegistered(Event e) {
        var s = e.getArgument(0, DTUPayUser.class);
        registeredMerchant.complete(s);
    }

    public Token getToken(TokenRequest t) {
        requestedToken = new CompletableFuture<>();
        Event event = new Event("TokenRequested", new Object[] { t });
        queue.publish(event);
        return requestedToken.join();
    }

    public void handleRequestedToken(Event e) {
        var s = e.getArgument(0, Token.class);
        requestedToken.complete(s);
    }

    public Response requestTransaction(Transaction t) {
        requestedTransaction = new CompletableFuture<>();
        Event event = new Event("TransactionRequested", new Object[] { t });
        queue.publish(event);
        if (requestedTransaction.join().equals("completed")){
            return Response.status(Response.Status.CREATED).build();
        }
        return Response.status(Response.Status.FORBIDDEN).build();
    }

    public void handleTransactionCompleted(Event e) {
        var s = e.getArgument(0, String.class);
        requestedTransaction.complete(s);
        // TODO standardize the response
    }

    // Helper functions
    public String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    public void completeFutureByCorrelationId(String correlationId, DTUPayUser result) {
        pendingCustomers.get(correlationId).complete(result);
        pendingCustomers.remove(correlationId);
    }

}
