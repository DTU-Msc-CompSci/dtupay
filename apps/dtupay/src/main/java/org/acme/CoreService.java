package org.acme;

import messaging.Event;
import messaging.MessageQueue;

import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Response;
import java.util.concurrent.CompletableFuture;

public class CoreService {
    private MessageQueue queue;
    private CompletableFuture<AccountResponse> registeredCustomer;
    private CompletableFuture<AccountResponse> registeredMerchant;
    private boolean tokenRemoved;
    private CompletableFuture<Boolean> deRegisteredCustomerCompleted;
    private CompletableFuture<Boolean> deRegisteredCustomerCompletedFailed;
    private CompletableFuture<Boolean> deRegisteredMerchantCompleted;
    private CompletableFuture<List<Transaction>> requestedCustomerReport;
    private boolean deRegisteredCustomer;

    private CompletableFuture<TokenResponse> requestedToken;
    private CompletableFuture<String> requestedTransaction;

    public CoreService(MessageQueue q) {
        queue = q;
        queue.addHandler("CustomerAccountCreated", this::handleCustomerRegistration);
        queue.addHandler("MerchantAccountCreated", this::handleMerchantRegistration);
        queue.addHandler("CustomerAccountCreationFailed", this::handleCustomerRegistration);
        queue.addHandler("MerchantAccountCreationFailed", this::handleMerchantRegistration);

        queue.addHandler("TokenRequestFulfilled", this::handleRequestedToken);
        queue.addHandler("TokenRequestFailed", this::handleTokenRequestFailed);

        queue.addHandler("TransactionCompleted", this::handleTransactionCompleted);
        queue.addHandler("TransactionFailed", this::handleTransactionFailed);
        queue.addHandler("CustomerAccountDeRegistrationCompleted", this::handleCustomerDeRegistrationCompleted);
        queue.addHandler("MerchantAccountDeRegistrationCompleted", this::handleMerchantDeRegistrationCompleted);
        queue.addHandler("AllTokenRemovedFromDeRegisteredCustomer", this::handleAllTokenRemovedFromDeRegisteredCustomer);
        queue.addHandler("CustomerAccountDeRegistrationFailed", this::handleCustomerDeRegistrationFailed);
        queue.addHandler("MerchantAccountDeRegistrationFailed", this::handleMerchantDeRegistrationFailed);

        //queue.addHandler("CustomerReportCreated", this::handleCustomerReportCreated);
    }

    public List<Transaction> getCustomerReport(String customerID) {
        requestedCustomerReport = new CompletableFuture<>();
        Event event = new Event("CustomerReportRequested", new Object[] { "0", customerID });
        queue.publish(event);
        return requestedCustomerReport.join();
    }
//    private void handleCustomerReportCreated(Event event) {
//        var s = event.getArgument(0, Boolean.class);
//         s = event.getArgument(1,Object.class);
//    }

    private void handleMerchantDeRegistrationFailed(Event event) {
        deRegisteredMerchantCompleted.complete(false);
    }

    private void handleCustomerDeRegistrationFailed(Event event) {
        var s = event.getArgument(0, Boolean.class);
        deRegisteredCustomerCompletedFailed.complete(s);
    }

    public void handleAllTokenRemovedFromDeRegisteredCustomer(Event ev) {
        tokenRemoved = ev.getArgument(0, Boolean.class);
        if(deRegisteredCustomer) {
            deRegisteredCustomerCompleted.complete(true);
        }
    }


    public Boolean deRegisterCustomer(DTUPayUser user) {
        deRegisteredCustomerCompleted = new CompletableFuture<>();
        Event event = new Event("CustomerAccountDeRegistrationRequested", new Object[] {user.getUniqueId()});
        queue.publish(event);
        return deRegisteredCustomerCompleted.join();
        //return "De-registration request sent";
    }

    public Boolean deRegisterMerchant(DTUPayUser user) {
        deRegisteredMerchantCompleted = new CompletableFuture<>();
        Event event = new Event("MerchantAccountDeRegistrationRequested", new Object[] {user.getUniqueId()});
        queue.publish(event);
        return deRegisteredMerchantCompleted.join();
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
        deRegisteredCustomerCompleted.complete(true);
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
        String response = requestedTransaction.join();
        if (response.equals("completed")){
            return Response.status(201).build();
        }
        return Response.status(400).entity(response).build();
    }

    public void handleTransactionCompleted(Event e) {
        var id = e.getArgument(0, String.class);

        var s = e.getArgument(1, String.class);
        requestedTransaction.complete(s);
        // TODO standardize the response

    }

    public void handleTransactionFailed(Event e) {
        var id = e.getArgument(0, String.class);

        var s = e.getArgument(1, String.class);
        requestedTransaction.complete(s);
        // TODO standardize the response
    }

}
