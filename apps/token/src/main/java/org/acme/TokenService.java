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
        this.queue.addHandler("InvalidateTokenRequested", this::handleInvalidateTokenRequested);
        this.queue.addHandler("TokenUserRequested", this::handleTokenUserAdd);
    }

    public void handleTokenRequested(Event ev) {
        var s = ev.getArgument(0, TokenRequest.class);
        Event event;
        if (!assignedTokens.containsKey(s.getCid())) {
            event = new Event("TokenRequestFulfilled", new Object[] { "User does not exist" });
        } else if (assignedTokens.get(s.getCid()).size() > 1) {
            event = new Event("TokenRequestFulfilled", new Object[] { "User already has more than 1 token" });
        } else if (s.getAmount() <= 0) {
            event = new Event("TokenRequestFulfilled", new Object[] { "Less than 1 token requested" });
        } else if(s.getAmount() > 5) {
            event = new Event("TokenRequestFulfilled", new Object[] { "More than 5 tokens requested" });
        } else if(assignedTokens.get(s.getCid()).size() + s.getAmount() > 6) {
            event = new Event("TokenRequestFulfilled", new Object[] { "Not enough tokens available" });
        } else {
            event = new Event("TokenRequestFulfilled", new Object[] { generateTokens(s) });
        }
        queue.publish(event);

    }

    public void handleTokenUserAdd(Event ev) {
        var s = ev.getArgument(0,String.class);
        assignedTokens.put(s,new HashSet<Token>());
    }

    public void handleInvalidateTokenRequested(Event ev) {
        var token = ev.getArgument(0, Token.class);
        var customerId = tokenToId.get(token.getToken());
        System.out.println(token.getToken());
        tokenToId.remove(token.getToken(),customerId);
        assignedTokens.remove(customerId,token);
        usedTokenPool.add(token.getToken());
        System.out.println(customerId);
        Event customerInfoEvent = new Event("CustomerInfoRequested", new Object[] { customerId });
        queue.publish(customerInfoEvent);
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
