package org.acme;

import messaging.Event;
import messaging.MessageQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.acme.*;
public class AccountService {

    List<DTUPayUser> customers = new ArrayList<>();
    List<DTUPayUser> merchants = new ArrayList<>();


    // For RabbitMQ stuffs
    MessageQueue queue;

    public AccountService(MessageQueue q) {
        this.queue = q;
        this.queue.addHandler("CustomerAccountCreationRequested", this::handleCustomerAccountCreationRequested);
        this.queue.addHandler("MerchantAccountCreationRequested", this::handleMerchantAccountCreationRequested);
        this.queue.addHandler("TransactionRequested", this::handleTransactionRequested);
        this.queue.addHandler("TokenValidated", this::handleTokenValidated);
        this.queue.addHandler("CustomerAccountDeRegistrationRequested", this::handleCustomerAccountDeRegistrationRequested);
        this.queue.addHandler("MerchantAccountDeRegistrationRequested", this::handleMerchantAccountDeRegistrationRequested);

    }


//    public List<DTUPayUser> getCustomers() {
//        return customers;
//    }

    public String getCustomer(String uniqueId) {
        String bankId = null;
        for(DTUPayUser d : customers) {
            if(d.getUniqueId().equals(uniqueId)) {
                bankId = d.getBankId().getBankAccountId();
            }
        }
        return bankId;
        //return customers.stream().filter( (user) -> user.getUniqueId().equals(uniqueId)).
        //        map(dtuPayUser -> dtuPayUser.getBankId().getBankAccountId()).findFirst();
    }

    public String getMerchant(String uniqueId) {
        String bankId = null;
        for(DTUPayUser d : merchants) {
            if(d.getUniqueId().equals(uniqueId)) {
                bankId = d.getBankId().getBankAccountId();
            }
        }
        return bankId;
    }

    public String addCustomer(DTUPayUser user) {
        //TODO Query external BankService

        user.setUniqueId(generateUniqueId());

        customers.add(user);
        System.out.println("DTU Pay User added to service");
        return user.getUniqueId();
    }

    public String addMerchant(DTUPayUser user) {
        //TODO Query external BankService

        user.setUniqueId(generateUniqueId());

        merchants.add(user);
        System.out.println("DTU Pay User added to service");
        return user.getUniqueId();
    }

    public void removeCustomer(String uniqueId) {
        customers.removeIf(user -> user.getUniqueId().equals(uniqueId));
    }

    public void removeMerchant(String uniqueId) {
        merchants.removeIf(user -> user.getUniqueId().equals(uniqueId));
    }

    public String generateUniqueId() {
        return UUID.randomUUID().toString();
    }



    private void handleMerchantAccountDeRegistrationRequested(Event ev) {
        var s = ev.getArgument(0, String.class);
        removeMerchant(s);
        Event event = new Event("MerchantAccountDeRegistrationCompleted", new Object[] {true});
        queue.publish(event);
        System.out.println("Merchant account de-registration completed");
    }

    private void handleCustomerAccountDeRegistrationRequested(Event ev) {
        var s = ev.getArgument(0, String.class);
        removeCustomer(s);
        Event event = new Event("CustomerAccountDeRegistrationCompleted", new Object[] {true});
        queue.publish(event);
        System.out.println("Customer account de-registration completed");
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
    public void handleTokenValidated(Event ev) {
        var id = ev.getArgument(0, String.class);
        var s = ev.getArgument(1, String.class);
        // Verify that the unique ID is set correct
        String test = getCustomer(s);
        Event event = new Event("CustomerInfoProvided", new Object[] { id, test });
        // This needs to respond to a different queue; which are interested in the "CustomerAccountCreated" topics
        // This is the "hat" that it wears
        queue.publish(event);

    }
    public void handleTransactionRequested(Event ev) {
        var id = ev.getArgument(0,String.class);
        var s = ev.getArgument(1, Transaction.class);
        // Verify that the unique ID is set correct
        String merchantId = getMerchant(s.getMerchantId());
        Event event = new Event("MerchantInfoProvided", new Object[] {id, merchantId });
        // This needs to respond to a different queue; which are interested in the "CustomerAccountCreated" topics
        // This is the "hat" that it wears
        queue.publish(event);

    }

    public void handleMerchantAccountCreationRequested(Event ev) {
        var s = ev.getArgument(0, DTUPayUser.class);
        // Verify that the unique ID is set correct
        addMerchant(s);
        Event event = new Event("MerchantAccountCreated", new Object[] { s });
        // This needs to respond to a different queue; which are interested in the "MerchantAccountCreated" topics
        // This is the "hat" that it wears
        queue.publish(event);

        // TODO: REMOVE ME
        System.out.println("Merchant Account Created");
    }



}