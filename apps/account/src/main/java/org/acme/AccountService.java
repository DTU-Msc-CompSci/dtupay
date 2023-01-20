package org.acme;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankServiceService;

import messaging.Event;
import messaging.MessageQueue;

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

    private DTUPayUser getCustomerInfo(String uniqueId) {
        DTUPayUser customer = null;
        for(DTUPayUser d : customers) {
            if(d.getUniqueId().equals(uniqueId)) {
                customer = d;
                break;
            }
        }
        return customer;
    }

    private DTUPayUser getMerchantInfo(String uniqueId) {
        DTUPayUser merchant = null;
        for(DTUPayUser d : merchants) {
            if(d.getUniqueId().equals(uniqueId)) {
                merchant = d;
                break;
            }
        }
        return merchant;
    }

    public boolean doesCustomerExist(String bankId){
        for(DTUPayUser d : customers) {
            if(d.getBankId().getBankAccountId().equals(bankId)) {
                return true;
            }
        }
        return false;
    }

    public boolean doesCustomerExistUniqueId(String uniqueId){
        //TODO Ask if this is going to be the uniqueId
        for(DTUPayUser d : customers) {
            if(d.getUniqueId().equals(uniqueId)) {
                return true;
            }
        }
        return false;
    }

    public boolean doesMerchantExist(String bankId ){
        for(DTUPayUser d : merchants) {
            if(d.getBankId().getBankAccountId().equals(bankId)) {
                return true;
            }
        }
        return false;
    }

    public boolean doesMerchantExistUniqueId(String unique ){
        //TODO Ask if this is going to be the uniqueId
        //Search by bankId which is unique for each account
        for(DTUPayUser d : merchants) {
            if(d.getUniqueId().equals(unique)) {
                return true;
            }
        }
        return false;
    }

    void addUser(DTUPayUser user, String userType) {
        user.setUniqueId(generateUniqueId());
        // TODO: I don't think we need to do COrrelation ID here
//        var correlationId = generateCorrelationId();

        if(userType.equals("customer")){
          customers.add(user);
          Event event = new Event("TokenUserRequested", new Object[] { user.getUniqueId() });
          queue.publish(event);
        }
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
        var correlationId = ev.getArgument(0, String.class);
        var user = ev.getArgument(1, DTUPayUser.class);
        Event event;
        try {
            bankService.getAccount(user.getBankId().getBankAccountId());
            if (doesMerchantExist(user.getBankId().getBankAccountId())){
                event = new Event("MerchantAccountCreationFailed", new Object[] { correlationId,  new AccountResponse(user,"Duplicate User")});
            } else{
                addUser(user, "merchant");
                event = new Event("MerchantAccountCreated", new Object[] { correlationId, new AccountResponse(user,"Success")});
            }
        } catch (BankServiceException_Exception e) {
            event = new Event("MerchantAccountCreationFailed", new Object[] { correlationId, new AccountResponse(user, "Invalid BankAccountId") });
        } catch (Exception e) {
            event = new Event("MerchantAccountCreationFailed", new Object[] { correlationId, new AccountResponse(user, "Unknown Error") });
        }
        queue.publish(event);
        return user.getUniqueId();
    }

    // TODO: Correlation ID will be the first field, and the Domain object/s will be from 2 onward
    public String handleCustomerAccountCreationRequested(Event ev) {
        var correlationId = ev.getArgument(0, String.class);
        var user = ev.getArgument(1, DTUPayUser.class);
        Event event = null;
        try {
            bankService.getAccount(user.bankId.bankAccountId);
            if (doesCustomerExist(user.getBankId().getBankAccountId())){
                event = new Event("CustomerAccountCreationFailed", new Object[] { correlationId, new AccountResponse(user, "Duplicate User") });
            } else {
                addUser(user,"customer");
                event = new Event("CustomerAccountCreated", new Object[]{ correlationId, new AccountResponse(user, "Success") });
            }
        } catch (BankServiceException_Exception e) {
            event = new Event("CustomerAccountCreationFailed", new Object[] { correlationId, new AccountResponse(user, "Invalid BankAccountId") });
        } catch (Exception e) {
            event = new Event("CustomerAccountCreationFailed", new Object[] { correlationId, new AccountResponse(user, "Unknown Error") });
        }
        queue.publish(event);
        return user.getUniqueId();
    }

    public void handleCustomerAccountDeRegistrationRequested(Event ev) {
        var correlationId = ev.getArgument(0, String.class);
        var s = ev.getArgument(1, String.class);
        Event event = null;
        if(doesCustomerExistUniqueId(s) && s != null){
            removeCustomer(s);
            event = new Event("CustomerAccountDeRegistrationCompleted", new Object[] { correlationId, true });
        }
        else {
            event = new Event("CustomerAccountDeRegistrationFailed", new Object[] { correlationId, false });
        }
        queue.publish(event);
    }

    public void handleMerchantAccountDeRegistrationRequested(Event ev) {
        var correlationId = ev.getArgument(0, String.class);
        var s = ev.getArgument(1, String.class);
        Event event = null;
        if(doesMerchantExistUniqueId(s)){
            removeMerchant(s);
            event = new Event("MerchantAccountDeRegistrationCompleted", new Object[] { correlationId, true });
        }
        else {
            event = new Event("MerchantAccountDeRegistrationFailed", new Object[] { correlationId, false });
        }
        queue.publish(event);
    }

    public void handleTokenValidated(Event ev) {
        var correlationId = ev.getArgument(0, String.class);
        var user = ev.getArgument(1, String.class);
        Event event = new Event("CustomerInfoProvided", new Object[] { correlationId, getCustomerInfo(user) });
        queue.publish(event);

    }

    public void handleTransactionRequested(Event ev) {
        var correlationId = ev.getArgument(0, String.class);
        var s = ev.getArgument(1, Transaction.class);
        Event event = new Event("MerchantInfoProvided", new Object[] { correlationId, getMerchantInfo(s.getMerchantId()) });
        queue.publish(event);
    }

    public String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
