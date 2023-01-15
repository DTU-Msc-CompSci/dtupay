package org.acme;

import messaging.Event;
import messaging.MessageQueue;

import java.util.*;

public class TokenService {
    Map<String, Token> assignedTokens = new HashMap<String,Token>();
    Map<String, String> tokenToId = new HashMap<String,String>();
    Set<String> usedTokenPool = new HashSet<String>();

    MessageQueue queue;
    public TokenService(MessageQueue q) {
        this.queue = q;
        this.queue.addHandler("TokenRequested", this::handleTokenRequested);
        this.queue.addHandler("InvalidateTokenRequested", this::handleInvalidateTokenRequested);

    }

    public void handleTokenRequested(Event ev) {
        var s = ev.getArgument(0, TokenRequest.class);
        Token token = generateToken(s);
        Event event = new Event("TokenRequestFulfilled", new Object[] { token });
        queue.publish(event);

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



    public Token generateToken(TokenRequest tokenRequest) {
        // TODO: Expand to a list of Tokens later
        String tokenId = UUID.randomUUID().toString();
        while (usedTokenPool.contains(tokenId)) {
            tokenId = UUID.randomUUID().toString();
        }
        System.out.println(tokenId);
        System.out.println(tokenRequest.getCid());
        Token t = new Token(tokenId);
        System.out.println(t.getToken());
        assignedTokens.put(tokenRequest.getCid(),t);
        tokenToId.put(t.getToken(),tokenRequest.getCid());
        System.out.println(tokenToId.get(t.getToken()));
        return t;
    }
}
