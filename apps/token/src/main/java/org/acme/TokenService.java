package org.acme;

import messaging.Event;
import messaging.MessageQueue;

import java.util.*;

public class TokenService {
    Map<String, Set<Token>> assignedTokens = new HashMap<String,Set<Token>>();
    Map<String, String> tokenToId = new HashMap<String,String>();
    Set<String> usedTokenPool = new HashSet<String>();

    MessageQueue queue;
    public Map<String, Set<Token>> getAssignedTokens() {
        return assignedTokens;
    }
    public TokenService(MessageQueue q) {
        this.queue = q;
        this.queue.addHandler("TokenRequested", this::handleTokenRequested);
        this.queue.addHandler("TransactionRequested", this::handleTransactionRequested);
        this.queue.addHandler("CustomerAccountDeRegistrationRequested", this::handleRemoveAllTokenFromDeRegisteredCustomer);
        this.queue.addHandler("TokenUserRequested", this::handleTokenUserAdd);
    }
    public void handleRemoveAllTokenFromDeRegisteredCustomer(Event ev) {
        removeAllTokenFromCustomer(ev.getArgument(0, String.class));
        Event event = new Event("AllTokenRemovedFromDeRegisteredCustomer", new Object[] {true});
        queue.publish(event);
    }

    public void handleTokenUserAdd(Event ev) {
        var s = ev.getArgument(0,String.class);
        assignedTokens.put(s,new HashSet<Token>());
    }

    public void handleTokenRequested(Event ev) {
        var correlationId = ev.getArgument(0, String.class);
        var s = ev.getArgument(1, TokenRequest.class);
        Event event;
        TokenResponse response = new TokenResponse();
        if (!assignedTokens.containsKey(s.getCid())) {
            response.setMessage("User does not exist");
        } else if (assignedTokens.get(s.getCid()).size() > 1) {
            response.setMessage("User already has more than 1 token");
        } else if (s.getAmount() <= 0) {
            response.setMessage("Less than 1 token requested");
        } else if(s.getAmount() > 5) {
            response.setMessage("More than 5 tokens requested");
        } else if(assignedTokens.get(s.getCid()).size() + s.getAmount() > 6) {
            response.setMessage("Not enough tokens available");
        } else {
            response.setMessage("success");
            response.setTokens(generateTokens(s));

        }

        if (response.getMessage() == null) {
            event = new Event("TokenRequestFailed", new Object[] { response });
        } else {
            event = new Event("TokenRequestFulfilled", new Object[] { response });
        }

        queue.publish(event);
    }

    public void handleTransactionRequested(Event ev) {
        var correlationId = ev.getArgument(0, String.class);
        var token = ev.getArgument(1, Transaction.class).getCustomerToken();
        var customerId = tokenToId.get(token.getToken());
        tokenToId.remove(token.getToken(),customerId);
        assignedTokens.remove(customerId,token);
        usedTokenPool.add(token.getToken());
        System.out.println(customerId);
        Event customerInfoEvent = new Event("TokenValidated", new Object[] { correlationId, customerId });
        queue.publish(customerInfoEvent);
    }

    public void removeAllTokenFromCustomer(String customerId) {
        assignedTokens.remove(customerId);
        while (tokenToId.values().remove(customerId));
    }

    public Set<Token> generateTokens(TokenRequest tokenRequest) {

        Set<Token> requestTokens = new HashSet<Token>();

        for (int i = 0; i < tokenRequest.getAmount(); i++) {
            //Set<Token> ts = assignedTokens.get(tokenRequest.getCid());
            String tokenId = UUID.randomUUID().toString();
            while (usedTokenPool.contains(tokenId)) {
                tokenId = UUID.randomUUID().toString();
            }
            Token t = new Token(tokenId);
            assignedTokens.get(tokenRequest.getCid()).add(t);
            tokenToId.put(t.getToken(),tokenRequest.getCid());
            requestTokens.add(t);
        }
        return requestTokens;
    }
}
