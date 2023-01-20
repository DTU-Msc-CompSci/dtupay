package org.acme.events;

import lombok.Getter;
import messaging.Event;

public abstract class PaymentEvent extends Event {


    private static long versionCount = 1;

    @Getter
    private final long version = versionCount++;

    public abstract String getTransactionID();

}
