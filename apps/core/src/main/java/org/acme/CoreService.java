package org.acme;

import jdk.jshell.SourceCodeAnalysis;
import messaging.Event;
import messaging.MessageQueue;

import javax.ws.rs.core.Response;
import java.util.concurrent.CompletableFuture;

public class CoreService {
    private MessageQueue queue;
    private CompletableFuture<DTUPayUser> registeredCustomer;
    private CompletableFuture<Token> requestedToken;
    private CompletableFuture<Transaction> requestedTransaction;

    public CoreService(MessageQueue q) {
        queue = q;
        queue.addHandler("CustomerAccountCreated", this::handleCustomerRegistered);
        queue.addHandler("TokenRequestFulfilled", this::handleRequestedToken);
    }

    public DTUPayUser registerCustomer(DTUPayUser c) {
        registeredCustomer = new CompletableFuture<>();
        Event event = new Event("CustomerAccountCreationRequested", new Object[] { c });
        queue.publish(event);
        return registeredCustomer.join();
    }

    public void handleCustomerRegistered(Event e) {
        var s = e.getArgument(0, DTUPayUser.class);
        registeredCustomer.complete(s);
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
        requestedTransaction.join();
        return Response.status(Response.Status.CREATED).build();
    }
}
