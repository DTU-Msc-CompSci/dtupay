package org.acme;

import messaging.Event;
import messaging.MessageQueue;

import javax.ws.rs.core.Response;
import java.util.concurrent.CompletableFuture;

public class CoreService {
    private MessageQueue queue;
    private CompletableFuture<AccountResponse> registeredCustomer;
    private CompletableFuture<AccountResponse> registeredMerchant;
    private boolean tokenRemoved;
    private CompletableFuture<Boolean> deRegisteredCustomerCompleted;
    private CompletableFuture<Boolean> deRegisteredMerchantCompleted;
    private boolean deRegisteredCustomer;

    private CompletableFuture<Token> requestedToken;
    private CompletableFuture<String> requestedTransaction;

    public CoreService(MessageQueue q) {
        queue = q;
        queue.addHandler("CustomerAccountCreated", this::handleCustomerRegistration);
        queue.addHandler("MerchantAccountCreated", this::handleMerchantRegistration);
        queue.addHandler("CustomerAccountCreationFailed", this::handleCustomerRegistration);
        queue.addHandler("MerchantAccountCreationFailed", this::handleMerchantRegistration);

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


    public String deRegisterCustomer(DTUPayUser user) {
        deRegisteredCustomerCompleted = new CompletableFuture<>();
        Event event = new Event("CustomerAccountDeRegistrationRequested", new Object[] {user.getUniqueId()});
        queue.publish(event);
        deRegisteredCustomerCompleted.join();
        return "De-registration request sent";
    }

    public String deRegisterMerchant(DTUPayUser user) {
        deRegisteredMerchantCompleted = new CompletableFuture<>();
        Event event = new Event("MerchantAccountDeRegistrationRequested", new Object[] {user.getUniqueId()});
        queue.publish(event);
        deRegisteredMerchantCompleted.join();
        return "De-registration request sent";
    }

    public AccountResponse registerCustomer(DTUPayUser c) {
        registeredCustomer = new CompletableFuture<>();
        Event event = new Event("CustomerAccountCreationRequested", new Object[] { c });
        queue.publish(event);
        return registeredCustomer.join();
    }
    public AccountResponse registerMerchant(DTUPayUser c) {
        registeredMerchant = new CompletableFuture<>();
        Event event = new Event("MerchantAccountCreationRequested", new Object[] { c });
        queue.publish(event);
        return registeredMerchant.join();
    }

    public void handleCustomerRegistration(Event e) {
        var s = e.getArgument(0, AccountResponse.class);
        registeredCustomer.complete(s);
    }
    public void handleMerchantRegistration(Event e) {
        var s = e.getArgument(0, AccountResponse.class);
        registeredMerchant.complete(s);
    }
    public void handleMerchantDeRegistrationCompleted(Event e) {
        var s = e.getArgument(0, Boolean.class);
        deRegisteredMerchantCompleted.complete(s);
    }

    public void handleCustomerDeRegistrationCompleted(Event event) {
        deRegisteredCustomer = event.getArgument(0, Boolean.class);
        if(tokenRemoved) {
            deRegisteredCustomerCompleted.complete(true);
        }
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

}
