package org.acme;

import messaging.Event;
import messaging.MessageQueue;

import java.util.concurrent.CompletableFuture;

public class CoreService {
    private MessageQueue queue;
    private CompletableFuture<DTUPayUser> registeredCustomer;

    public CoreService(MessageQueue q) {
        queue = q;
        queue.addHandler("CustomerAccountCreated", this::handleCustomerRegistered);
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
}
