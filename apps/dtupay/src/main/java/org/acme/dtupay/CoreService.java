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
    private Map<String, CompletableFuture<AccountResponse>> pendingCustomers = new ConcurrentHashMap<>();

    private Map<String, CompletableFuture<Boolean>> pendingDeregisterCustomers = new ConcurrentHashMap<>();
    private Map<String, CompletableFuture<Boolean>> pendingDeregisterMerchants = new ConcurrentHashMap<>();
    private CompletableFuture<AccountResponse> registeredCustomer;
    private CompletableFuture<AccountResponse> registeredMerchant;
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
        // Where did these go?????
//        queue.addHandler("CustomerAccountCreationFailed", this::handleCustomerRegistration);
//        queue.addHandler("MerchantAccountCreationFailed", this::handleMerchantRegistration);

        queue.addHandler("TokenRequestFulfilled", this::handleRequestedToken);
        queue.addHandler("TokenRequestFailed", this::handleTokenRequestFailed);

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

    public Map<String, CompletableFuture<AccountResponse>> getPendingCustomers() {
        return pendingCustomers;
    }

    public Boolean deRegisterCustomer(DTUPayUser user) {
        deRegisteredCustomerCompleted = new CompletableFuture<>();
        Event event = new Event("CustomerAccountDeRegistrationRequested", new Object[] {correlationId, user.getUniqueId()});
        queue.publish(event);
        return deRegisteredCustomerCompleted.join();
        //return "De-registration request sent";
    }

    public Boolean deRegisterMerchant(DTUPayUser user) {
        deRegisteredMerchantCompleted = new CompletableFuture<>();
        Event event = new Event("MerchantAccountDeRegistrationRequested", new Object[] {correlationId, user.getUniqueId()});
        queue.publish(event);
        return deRegisteredMerchantCompleted.join();
    }

    // TODO: All the events that are going to be generating the Correlation ID need to follow this pattern
    public AccountResponse registerCustomer(DTUPayUser c) {
        CompletableFuture<AccountResponse> registeredCustomer = new CompletableFuture<>();
        var correlationId = generateCorrelationId();
        pendingCustomers.put(correlationId, registeredCustomer);
        Event event = new Event("CustomerAccountCreationRequested", new Object[] { correlationId, c });
        queue.publish(event);
        return registeredCustomer.join();
    }

    public AccountResponse registerMerchant(DTUPayUser c) {
        registeredMerchant = new CompletableFuture<>();
        var correlationId = UUID.randomUUID().toString();
        Event event = new Event("MerchantAccountCreationRequested", new Object[] { correlationId, c });
        queue.publish(event);
        return registeredMerchant.join();
    }

    public void handleCustomerRegistered(Event e) {
        var correlationId = e.getArgument(0, String.class);
        var s = e.getArgument(1, AccountResponse.class);
        completeFutureByCorrelationId(correlationId, s);
    }
    public void handleMerchantRegistered(Event e) {
        var correlationId = e.getArgument(0, String.class);
        var s = e.getArgument(1, AccountResponse.class);
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

    public void handleCustomerDeRegistrationCompleted(Event event) {
        deRegisteredCustomer = event.getArgument(0, Boolean.class);
        deRegisteredCustomerCompleted.complete(true);
        var correlationId = event.getArgument(0, String.class);
        var s = e.getArgument(1, Boolean.class);
        // TODO: Implement the part of the Message object
        pendingDeregisterCustomers.get(correlationId).complete(s);
        pendingDeregisterCustomers.remove(correlationId);
        //TODO: check if tokenRemoved is true
    }


    public TokenResponse getToken(TokenRequest t) {
        requestedToken = new CompletableFuture<>();
        Event event = new Event("TokenRequested", new Object[] { t });
        queue.publish(event);
        return requestedToken.join();
    }

    public void handleRequestedToken(Event e) {
        TokenResponse s = e.getArgument(0, TokenResponse.class);
        requestedToken.complete(s);
    }

    public void handleTokenRequestFailed(Event ev) {
        TokenResponse s = ev.getArgument(0, TokenResponse.class);
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
        var id = e.getArgument(0, String.class);

        var s = e.getArgument(1, String.class);
        requestedTransaction.complete(s);
        // TODO standardize the response
    }

    // Helper functions
    public String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    public void completeFutureByCorrelationId(String correlationId, AccountResponse result) {
        pendingCustomers.get(correlationId).complete(result);
        pendingCustomers.remove(correlationId);
    }

}
