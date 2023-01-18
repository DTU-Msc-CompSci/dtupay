package org.acme;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankServiceService;

import messaging.Event;
import messaging.MessageQueue;

import java.util.ArrayList;
import java.util.List;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;


public class AccountService {


    private BankService bankService =  new BankServiceService().getBankServicePort();

    //List<DTUPayUser> customers = new ArrayList<>();
    //List<DTUPayUser> merchants = new ArrayList<>();

    private CopyOnWriteArrayList<DTUPayUser> customers = new CopyOnWriteArrayList<DTUPayUser>();
    private CopyOnWriteArrayList<DTUPayUser> merchants = new CopyOnWriteArrayList<DTUPayUser>();


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

    private String getCustomerBankInfo(String uniqueId) {
        String bankId = null;
        for(DTUPayUser d : customers) {
            if(d.getUniqueId().equals(uniqueId)) {
                bankId = d.getBankId().getBankAccountId();
                break;
            }
        }
        return bankId;
    }

    private String getMerchantBankInfo(String uniqueId) {
        String bankId = null;
        for(DTUPayUser d : merchants) {
            if(d.getUniqueId().equals(uniqueId)) {
                bankId = d.getBankId().getBankAccountId();
            }
        }
        return bankId;
    }

    public boolean doesCustomerExist(String bankId){
        //TODO Ask if this is going to be the uniqueId
        for(DTUPayUser d : customers) {
            if(d.getBankId().getBankAccountId().equals(bankId)) {
                return true;
            }
        }
        return false;
    }

    public boolean doesMerchantExist(String bankId ){
        //TODO Ask if this is going to be the uniqueId
        //Search by bankId which is unique for each account
        for(DTUPayUser d : merchants) {
            if(d.getBankId().getBankAccountId().equals(bankId)) {
                return true;
            }
        }
        return false;
    }

    private void addUser(DTUPayUser user, String userType) {
        user.setUniqueId(generateUniqueId());
        if(userType.equals("customer")){ customers.add(user); }
        else if(userType.equals("merchant")){ merchants.add(user); }
        System.out.println("DTU Pay User added to service");
    }

    private void removeCustomer(String uniqueId) {
        customers.removeIf(user -> user.getUniqueId().equals(uniqueId));
    }

    private void removeMerchant(String uniqueId) {
        merchants.removeIf(user -> user.getUniqueId().equals(uniqueId));
    }

    private String generateUniqueId() {
        return UUID.randomUUID().toString();
    }


    public String handleMerchantAccountCreationRequested(Event ev) {
        var user = ev.getArgument(0, DTUPayUser.class);
        Event event;
        try {
            bankService.getAccount(user.getBankId().getBankAccountId());
            if (doesMerchantExist(user.getBankId().getBankAccountId())){
                event = new Event("MerchantAccountCreationFailed", new Object[] { new AccountResponse(user,"Duplicate User")});
            } else{
                addUser(user, "merchant");
                event = new Event("MerchantAccountCreated", new Object[] { new AccountResponse(user,"Success")});
            }
        } catch (BankServiceException_Exception e) {
            event = new Event("MerchantAccountCreationFailed", new Object[] { new AccountResponse(user,"Invalid BankAccountId")});
        }
        queue.publish(event);
        return user.getUniqueId();
    }

    public String handleCustomerAccountCreationRequested(Event ev) {
        var user = ev.getArgument(0, DTUPayUser.class);
        Event event;
        try {
            bankService.getAccount(user.getBankId().getBankAccountId());
            if (doesCustomerExist(user.getBankId().getBankAccountId())){
                event = new Event("CustomerAccountCreationFailed", new Object[] { new AccountResponse(user, "Duplicate User")});
            } else {
                addUser(user,"customer");
                event = new Event("CustomerAccountCreated", new Object[] { new AccountResponse(user, "Success")});
            }
        } catch (BankServiceException_Exception e) {
            event = new Event("CustomerAccountCreationFailed", new Object[] { new AccountResponse(user, "Invalid BankAccountId")});
        }

        queue.publish(event);
        return user.getUniqueId();
    }

    public void handleCustomerAccountDeRegistrationRequested(Event ev) {
        var s = ev.getArgument(0, String.class);
        removeCustomer(s);
        Event event = new Event("CustomerAccountDeRegistrationCompleted");
        queue.publish(event);
    }

    public void handleMerchantAccountDeRegistrationRequested(Event ev) {
        var s = ev.getArgument(0, String.class);
        removeMerchant(s);
        Event event = new Event("MerchantAccountDeRegistrationCompleted");
        queue.publish(event);
    }

    public void handleTokenValidated(Event ev) {
        var id = ev.getArgument(0, String.class);
        var user = ev.getArgument(1, String.class);
        Event event = new Event("CustomerInfoProvided", new Object[] { id, getCustomerBankInfo(user) });
        queue.publish(event);

    }

    public void handleTransactionRequested(Event ev) {
        var id = ev.getArgument(0, String.class);

        var s = ev.getArgument(1, Transaction.class);
        Event event = new Event("MerchantInfoProvided", new Object[] { id, getMerchantBankInfo(s.getMerchantId()) });
        queue.publish(event);
    }
}