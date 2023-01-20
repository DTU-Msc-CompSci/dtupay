package org.acme.repositories;

import lombok.NonNull;
import messaging.Event;
import messaging.MessageQueue;
import org.acme.events.PaymentEvent;
import org.acme.events.TransactionCreated;
import org.acme.events.TransactionCustomerInfoAdded;
import org.acme.events.TransactionMerchantInfoAdded;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class EventStore {

    private final Map<String, List<PaymentEvent>> store = new ConcurrentHashMap<>();

    private final MessageQueue eventBus;

    public EventStore(MessageQueue bus) {
        this.eventBus = bus;
    }

    public void addEvent(String id, PaymentEvent event) {
        if (event instanceof TransactionCreated) {
            addEvent(id, (TransactionCreated) event);
        }
        if (event instanceof TransactionCustomerInfoAdded) {
            addEvent(id, (TransactionCustomerInfoAdded) event);
        }
        if (event instanceof TransactionMerchantInfoAdded) {
            addEvent(id, (TransactionMerchantInfoAdded) event);
        }
    }

    public void addEvent(String id, TransactionCreated event) {
        if (!store.containsKey(event.getTransactionID())) {
            store.put(event.getTransactionID(), new ArrayList<>());
        }
        store.get(event.getTransactionID()).add(event);
        var globalEvent = new Event("TransactionCreated", new Object[]{event.getTransactionID(), event.getCustomerToken(), event.getMerchantID(), event.getAmount()});

		eventBus.publish(globalEvent);
	}
	public void addEvent(String id, TransactionCustomerInfoAdded event) {
		if (!store.containsKey(event.getTransactionID())) {
			store.put(event.getTransactionID(), new ArrayList<PaymentEvent>());
		}
		store.get(event.getTransactionID()).add(event);

		Event globalEvent = new Event("TransactionCustomerInfoAdded", new Object[] { event.getTransactionID(), event.getCustomerInfo() });

		eventBus.publish(globalEvent);
	}
	public void addEvent(String id, TransactionMerchantInfoAdded event) {
		if (!store.containsKey(event.getTransactionID())) {
			store.put(event.getTransactionID(), new ArrayList<PaymentEvent>());
		}
		store.get(event.getTransactionID()).add(event);
		var globalEvent = new Event("TransactionMerchantInfoAdded", new Object[] { event.getTransactionID(), event.getMerchantInfo() });

		eventBus.publish(globalEvent);
	}

    public Stream<PaymentEvent> getEventsFor(String id) {
        if (!store.containsKey(id)) {
            store.put(id, new ArrayList<PaymentEvent>());
        }
        return store.get(id).stream();
    }

    public void addEvents(@NonNull String userid, List<PaymentEvent> appliedEvents) {
        appliedEvents.forEach(e -> addEvent(userid, e));
    }

}
