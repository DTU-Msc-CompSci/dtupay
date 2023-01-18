package org.acme.aggregate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import messaging.Event;
import org.acme.events.PaymentEvent;
import org.acme.events.TransactionCreated;
import org.acme.events.TransactionCustomerBankIDAdded;
import org.acme.events.TransactionMerchantBankIDAdded;

@Getter
public class Payment {
	private String transactionID;
	private String customerBankID;
	private String merchantBankID;
	private String customerToken ;
	private String merchantID;
    private BigDecimal amount;


	@Setter(AccessLevel.NONE)
	private List<PaymentEvent> appliedEvents = new ArrayList<PaymentEvent>();

	private Map<Class<? extends Event>, Consumer<Event>> handlers = new HashMap<>();

	public void create(String transactionId,String customerToken, String merchantID, BigDecimal amount) {

		//var transactionId = UUID.randomUUID().toString();
		//var payment = new Payment();
        //payment.transactionID = transactionId;
		appliedEvents.add( (PaymentEvent)new TransactionCreated(transactionId, customerToken, customerToken,amount));

		applyEvents(appliedEvents.stream());


	}

	public static Payment createFromEvents(Stream<PaymentEvent> events) {
		Payment payment = new Payment();
		payment.applyEvents(events);
		return payment;
	}

	public Payment() {
		registerEventHandlers();
	}

	private void registerEventHandlers() {
		handlers.put(TransactionCreated.class, e -> apply((TransactionCreated) e));
		handlers.put(TransactionMerchantBankIDAdded.class, e -> apply((TransactionMerchantBankIDAdded) e));
		handlers.put(TransactionCustomerBankIDAdded.class, e -> apply((TransactionCustomerBankIDAdded) e));
	}

	/* Business Logic */

	public void addMerchantBankID(String transactionID, String merchantBankID) {
		appliedEvents.add( (PaymentEvent)new TransactionMerchantBankIDAdded(transactionID,merchantBankID));
        applyEvents(appliedEvents.stream());

    }
    public void addCustomerBankID(String transactionID, String customerBankID) {
        appliedEvents.add( (PaymentEvent)new TransactionCustomerBankIDAdded(transactionID,customerBankID));
        applyEvents(appliedEvents.stream());

    }



	/* PaymentEvent Handling */

	private void applyEvents(Stream<PaymentEvent> events) throws Error {
		events.forEachOrdered(e -> {
			this.applyEvent(e);
		});
//		if (this.getTransactionID() == null) {
//			throw new Error("Transaction does not exist");
//		}
	}

	private void applyEvent(PaymentEvent e) {
		System.out.println("APPLYING EVENT");
		handlers.getOrDefault(e.getClass(), this::missingHandler).accept(e);
	}

	private void missingHandler(Event e) {
		throw new Error("handler for event "+e+" missing");
	}

	private void apply(TransactionCreated event) {
		transactionID = event.getTransactionID();
		customerToken = event.getCustomerToken();
		merchantID = event.getMerchantID();
		amount = event.getAmount();
	}

	private void apply(TransactionMerchantBankIDAdded event) {
		transactionID = event.getTransactionID();
		merchantBankID = event.getMerchantBankID();
	}

	private void apply(TransactionCustomerBankIDAdded event) {

		transactionID = event.getTransactionID();
		customerBankID = event.getCustomerBankID();
	}

	public void clearAppliedEvents() {
		appliedEvents.clear();
	}
	public boolean complete(){
		return customerBankID != null && merchantBankID != null && amount!= null;
	}

}
