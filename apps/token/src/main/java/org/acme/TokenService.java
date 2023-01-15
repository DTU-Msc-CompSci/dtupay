package org.acme;

import messaging.Event;
import messaging.MessageQueue;

import java.util.*;

public class TokenService {
    Map<String, Token> assignedTokens = new HashMap<String,Token>();
    Map<Token, String> tokenToId = new HashMap<Token,String>();
    Set<Token> usedTokenPool = new HashSet<Token>();

    MessageQueue queue;
    public TokenService(MessageQueue q) {
        this.queue = q;
        this.queue.addHandler("TokenRequested", this::handleTokenRequested);
    }

    public void handleTokenRequested(Event ev) {
        var s = ev.getArgument(0, TokenRequest.class);
        Token token = generateToken(s);
        Event event = new Event("TokenRequestFulfilled", new Object[] { token });
        queue.publish(event);

    }

    public Token generateToken(TokenRequest tokenRequest) {
        // TODO: Expand to a list of Tokens later
        String tokenId = UUID.randomUUID().toString();
        while (usedTokenPool.contains(tokenId)) {
            tokenId = UUID.randomUUID().toString();
        }
        Token t = new Token(tokenId);
        assignedTokens.put(tokenRequest.getCid(),t);
        tokenToId.put(t,tokenRequest.getCid());
        return t ;
    }
}
