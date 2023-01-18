package org.acme;

import messaging.Event;
import messaging.MessageQueue;

import java.util.*;

public class TokenService {
    Map<String, Set<Token>> assignedTokens = new HashMap<String,Set<Token>>();
    Map<String, String> tokenToId = new HashMap<String,String>();
    Set<String> usedTokenPool = new HashSet<String>();

    MessageQueue queue;
    public TokenService(MessageQueue q) {
        this.queue = q;
        this.queue.addHandler("TokenRequested", this::handleTokenRequested);
        this.queue.addHandler("TransactionRequested", this::handleTransactionRequested);
        this.queue.addHandler("CustomerAccountDeRegistrationRequested", this::handleRemoveAllTokenFromDeRegisteredCustomer);
        // TODO: Implement this
//        this.queue.addHandler("InvalidateTokenRequested", this::handleInvalidateTokenRequested);
        this.queue.addHandler("TokenUserRequested", this::handleTokenUserAdd);
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
        }


        if (response.getMessage() != null) {
            System.out.println("TESTING!!!!!!!!!!!!!!!");
            queue.publish(new Event("TokenRequestFulfilled", new Object[] { correlationId, response }));

        } else {
            response.setMessage("success");
            response.setTokens(generateTokens(s));
            System.out.println("TESTING!!!!!!!!!!!!!!!");
            System.out.println(response.getMessage());
            System.out.println(response.getTokens());
            queue.publish(new Event("TokenRequestFulfilled", new Object[] { correlationId, response }));
            System.out.println("HERE");
        }
    }

    public void handleTokenUserAdd(Event ev) {
        var correlationId = ev.getArgument(0, String.class);
        var s = ev.getArgument(1,String.class);
        assignedTokens.put(s,new HashSet<Token>());
        // TODO: This needs a "TokensAddedToUser" event
    }
    public void handleRemoveAllTokenFromDeRegisteredCustomer(Event ev) {
        var correlationId = ev.getArgument(0, String.class);
        removeAllTokenFromCustomer(ev.getArgument(1, String.class));
        Event event = new Event("AllTokenRemovedFromDeRegisteredCustomer", new Object[] {correlationId, true});
        queue.publish(event);
    }

    public void handleTransactionRequested(Event ev) {
        var correlationId = ev.getArgument(0, String.class);
        var token = ev.getArgument(1, Transaction.class).getCustomerToken();
        var customerId = tokenToId.get(token.getToken());
        System.out.println(token.getToken());
        tokenToId.remove(token.getToken(),customerId);
        assignedTokens.remove(customerId,token);
        usedTokenPool.add(token.getToken());
        System.out.println(customerId);
        Event customerInfoEvent = new Event("TokenValidated", new Object[] { correlationId, customerId });
        queue.publish(customerInfoEvent);
    }

    public void removeAllTokenFromCustomer(String customerId) {
        for (Map.Entry<String, Token> entry : assignedTokens.entrySet()) {
            if (entry.getKey().equals(customerId)) {
                assignedTokens.remove(entry.getKey());
                tokenToId.remove(entry.getValue().getToken());
                usedTokenPool.add(entry.getValue().getToken());
            }
        }
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
