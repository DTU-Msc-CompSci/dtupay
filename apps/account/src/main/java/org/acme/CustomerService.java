package org.acme;

import messaging.Event;
import messaging.MessageQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CustomerService {

    List<DTUPayUser> users = new ArrayList<>();

    // For RabbitMQ stuffs
    MessageQueue queue;

    public CustomerService() {
//        System.out.println("CustomerService Created");
    }

    public List<DTUPayUser> getCustomers() {
        return users;
    }

    public Optional<DTUPayUser> getCustomer(String uniqueId) {
        return users.stream().filter( (user) -> user.getUniqueId().toString().equals(uniqueId)).findFirst();
    }

    public void addCustomer(DTUPayUser user) {
        //TODO Query external BankService

        user.setUniqueId(generateUniqueId());

        users.add(user);
        System.out.println("DTU Pay User added to service");
    }

    public String generateUniqueId() {
        return UUID.randomUUID().toString();
    }

    public CustomerService(MessageQueue q) {
        this.queue = q;
        this.queue.addHandler("CustomerAccountCreationRequested", this::handleCustomerAccountCreationRequested);
        this.queue.addHandler("CustomerAccountCreated", this::handleCustomerAccountCreated);
    }

    public void handleCustomerAccountCreationRequested(Event ev) {
        var s = ev.getArgument(0, DTUPayUser.class);
        // Verify that the unique ID is set correct
        addCustomer(s);
        Event event = new Event("CustomerAccountCreated", new Object[] { s });
        // This needs to respond to a different queue; which are interested in the "CustomerAccountCreated" topics
        // This is the "hat" that it wears
        queue.publish(event);

        // TODO: REMOVE ME
        System.out.println("Customer Account Created");
    }
    public void handleCustomerAccountCreated(Event ev) {
        var s = ev.getArgument(0, DTUPayUser.class);

        // TODO: REMOVE ME
        System.out.println("I have consumed myself...");
    }

}