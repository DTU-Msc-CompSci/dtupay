package org.acme;

import messaging.Event;
import messaging.MessageQueue;

import javax.ws.rs.core.Response;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class CoreService {
    private MessageQueue queue;
    private CompletableFuture<DTUPayUser> registeredCustomer;
    private CompletableFuture<DTUPayUser> registeredMerchant;

    private CompletableFuture<Object> requestedToken;
    private CompletableFuture<String> requestedTransaction;

    public CoreService(MessageQueue q) {
        queue = q;
        queue.addHandler("CustomerAccountCreated", this::handleCustomerRegistered);
        queue.addHandler("MerchantAccountCreated", this::handleMerchantRegistered);

        queue.addHandler("TokenRequestFulfilled", this::handleRequestedToken);
        queue.addHandler("TransactionCompleted", this::handleTransactionCompleted);

    }



    public DTUPayUser registerCustomer(DTUPayUser c) {
        registeredCustomer = new CompletableFuture<>();
        Event event = new Event("CustomerAccountCreationRequested", new Object[] { c });
        queue.publish(event);
        return registeredCustomer.join();
    }
    public DTUPayUser registerMerchant(DTUPayUser c) {
        registeredMerchant = new CompletableFuture<>();
        Event event = new Event("MerchantAccountCreationRequested", new Object[] { c });
        queue.publish(event);
        return registeredMerchant.join();
    }

    public void handleCustomerRegistered(Event e) {
        var s = e.getArgument(0, DTUPayUser.class);
        registeredCustomer.complete(s);
    }
    public void handleMerchantRegistered(Event e) {
        var s = e.getArgument(0, DTUPayUser.class);
        registeredMerchant.complete(s);
    }

    public Object getToken(TokenRequest t) {
        requestedToken = new CompletableFuture<>();
        Event event = new Event("TokenRequested", new Object[] { t });
        queue.publish(event);
        return requestedToken.join();
    }

    public void handleRequestedToken(Event e) {
        var s = e.getArgument(0, Object.class);
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
