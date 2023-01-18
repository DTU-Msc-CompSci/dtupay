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

public class CoreService {
    private MessageQueue queue;


    // TODO: Migrate these concurrent-safe collection
    private Map<String, CompletableFuture<DTUPayUser>> pendingCustomers = new ConcurrentHashMap<>();

    private Map<String, CompletableFuture<Boolean>> pendingDeregisterCustomers = new ConcurrentHashMap<>();
    private Map<String, CompletableFuture<Boolean>> pendingDeregisterMerchants = new ConcurrentHashMap<>();
    private CompletableFuture<DTUPayUser> registeredMerchant;
    private boolean tokenRemoved;
    private CompletableFuture<Boolean> deRegisteredCustomerCompleted;
    private CompletableFuture<Boolean> deRegisteredMerchantCompleted;
    private boolean deRegisteredCustomer;

    private CompletableFuture<TokenResponse> requestedToken;
    private CompletableFuture<String> requestedTransaction;

    public CoreService(MessageQueue q) {
        queue = q;
        queue.addHandler("CustomerAccountCreated", this::handleCustomerRegistered);
        queue.addHandler("MerchantAccountCreated", this::handleMerchantRegistered);

        queue.addHandler("TokenRequestFulfilled", this::handleRequestedToken);
        queue.addHandler("TransactionCompleted", this::handleTransactionCompleted);
        queue.addHandler("CustomerAccountDeRegistrationCompleted", this::handleCustomerDeRegistrationCompleted);
        queue.addHandler("MerchantAccountDeRegistrationCompleted", this::handleMerchantDeRegistrationCompleted);
        queue.addHandler("AllTokenRemovedFromDeRegisteredCustomer", this::handleAllTokenRemovedFromDeRegisteredCustomer);
    }

    public void handleAllTokenRemovedFromDeRegisteredCustomer(Event ev) {
        tokenRemoved = ev.getArgument(0, Boolean.class);
        if(deRegisteredCustomer) {
            deRegisteredCustomerCompleted.complete(true);
        }
    }

    public Map<String, CompletableFuture<DTUPayUser>> getPendingCustomers() {
        return pendingCustomers;
    }

    
    public String deRegisterCustomer(DTUPayUser user) {
        var correlationId = generateCorrelationId();
        deRegisteredCustomerCompleted = new CompletableFuture<>();
        Event event = new Event("CustomerAccountDeRegistrationRequested", new Object[] {correlationId, user.getUniqueId()});
        queue.publish(event);
        pendingDeregisterCustomers.put(correlationId, deRegisteredCustomerCompleted);
        deRegisteredCustomerCompleted.join();
        return "De-registration request sent";
    }

    public String deRegisterMerchant(DTUPayUser user) {
        var correlationId = generateCorrelationId();
        deRegisteredMerchantCompleted = new CompletableFuture<>();
        Event event = new Event("MerchantAccountDeRegistrationRequested", new Object[] {correlationId, user.getUniqueId()});
        queue.publish(event);
        pendingDeregisterMerchants.put(correlationId, deRegisteredMerchantCompleted);
        deRegisteredMerchantCompleted.join();
        return "De-registration request sent";
    }

    // TODO: All the events that are going to be generating the Correlation ID need to follow this pattern
    public DTUPayUser registerCustomer(DTUPayUser c) {
        CompletableFuture<DTUPayUser> registeredCustomer = new CompletableFuture<>();
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
        var correlationId = e.getArgument(0, String.class);
        var s = e.getArgument(1, DTUPayUser.class);
        completeFutureByCorrelationId(correlationId, s);
//        registeredMerchant.complete(correlationId, s);
    }
    public void handleMerchantDeRegistrationCompleted(Event e) {
        var correlationId = e.getArgument(0, String.class);
        var s = e.getArgument(1, Boolean.class);
        // TODO: Implement the part of the Message object
        pendingDeregisterCustomers.get(correlationId).complete(s);
        pendingDeregisterCustomers.remove(correlationId);
//        deRegisteredMerchantCompleted.complete(s);
    }

    public void handleCustomerDeRegistrationCompleted(Event e) {
        var correlationId = e.getArgument(0, String.class);
        var s = e.getArgument(1, Boolean.class);
        // TODO: Implement the part of the Message object
        pendingDeregisterCustomers.get(correlationId).complete(s);
        pendingDeregisterCustomers.remove(correlationId);
    }



    public TokenResponse getToken(TokenRequest t) {
        requestedToken = new CompletableFuture<>();
        Event event = new Event("TokenRequested", new Object[] { t });
        queue.publish(event);
        return requestedToken.join();
    }

    public void handleRequestedToken(Event e) {
        TokenResponse s = e.getArgument(0, TokenResponse.class);
        System.out.println("TESTING!!!!");
        System.out.println(s.getMessage());
        System.out.println(s.getTokens());
        requestedToken.complete(s);
    }

    public Response requestTransaction(Transaction t) {
        requestedTransaction = new CompletableFuture<>();
        Event event = new Event("TransactionRequested", new Object[] { UUID.randomUUID() ,t });
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
