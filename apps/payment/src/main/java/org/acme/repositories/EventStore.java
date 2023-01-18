package org.acme.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import lombok.NonNull;
import messaging.Event;
import messaging.MessageQueue;
import org.acme.events.PaymentEvent;
import org.acme.events.TransactionCreated;
import org.acme.events.TransactionCustomerBankIDAdded;
import org.acme.events.TransactionMerchantBankIDAdded;

public class EventStore {

	private Map<String, List<PaymentEvent>> store = new ConcurrentHashMap<>();

	private MessageQueue eventBus;

	public EventStore(MessageQueue bus) {
		this.eventBus = bus;
	}

	public void addEvent(String id, PaymentEvent event) {
		if (event instanceof TransactionCreated){
			addEvent(id, (TransactionCreated) event);
		}
		if (event instanceof TransactionMerchantBankIDAdded){
			addEvent(id, (TransactionMerchantBankIDAdded) event);
		}
		if (event instanceof TransactionCustomerBankIDAdded){
			addEvent(id, (TransactionCustomerBankIDAdded) event);
		}
	}

	public void addEvent(String id, TransactionCreated event) {
		if (!store.containsKey(event.getTransactionID())) {
			store.put(event.getTransactionID(), new ArrayList<PaymentEvent>());
		}
		store.get(event.getTransactionID()).add(event);
		var globalEvent = new Event("TransactionCreated", new Object[] { event.getTransactionID(), event.getCustomerToken(),event.getMerchantID(),event.getAmount()});

		eventBus.publish(event);
	}
	public void addEvent(String id, TransactionCustomerBankIDAdded event) {
		if (!store.containsKey(event.getTransactionID())) {
			store.put(event.getTransactionID(), new ArrayList<PaymentEvent>());
		}
		store.get(event.getTransactionID()).add(event);
		var globalEvent = new Event("TransactionCustomerBankIDAdded", new Object[] { event.getTransactionID(), event.getCustomerBankID() });

		eventBus.publish(event);
	}
	public void addEvent(String id, TransactionMerchantBankIDAdded event) {
		if (!store.containsKey(event.getTransactionID())) {
			store.put(event.getTransactionID(), new ArrayList<PaymentEvent>());
		}
		store.get(event.getTransactionID()).add(event);
		var globalEvent = new Event("TransactionMerchantBankIDAdded", new Object[] { event.getTransactionID(), event.getMerchantBankID() });

		eventBus.publish(event);
	}

	public Stream<PaymentEvent> getEventsFor(String id) {
		if (!store.containsKey(id)) {
			store.put(id, new ArrayList<PaymentEvent>());
		}
		return store.get(id).stream();
	}

	public void addEvents(@NonNull String userid, List<PaymentEvent> appliedEvents) {
		appliedEvents.stream().forEach(e -> addEvent(userid, e));
	}

}
