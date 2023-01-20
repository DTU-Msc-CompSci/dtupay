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
import org.acme.events.TransactionCustomerInfoAdded;
import org.acme.events.TransactionMerchantInfoAdded;

@Getter
public class Payment {
	private String transactionID;
	private String customerBankID;
	private String merchantBankID;
	private String customerToken ;
	private String merchantID;
    private BigDecimal amount;
	private Person merchant;
	private Person customer;
	private String customerID;



	@Setter(AccessLevel.NONE)
	private List<PaymentEvent> appliedEvents = new ArrayList<PaymentEvent>();

	private Map<Class<? extends Event>, Consumer<Event>> handlers = new HashMap<>();

	public void create(String transactionId,String customerToken, String merchantID, BigDecimal amount) {

		//var transactionId = UUID.randomUUID().toString();
		//var payment = new Payment();
        //payment.transactionID = transactionId;
		appliedEvents.add( (PaymentEvent)new TransactionCreated(transactionId, customerToken, merchantID,amount));

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
		handlers.put(TransactionMerchantInfoAdded.class, e -> apply((TransactionMerchantInfoAdded) e));
		handlers.put(TransactionCustomerInfoAdded.class, e -> apply((TransactionCustomerInfoAdded) e));
	}

	/* Business Logic */

	public void addMerchantInfo(String transactionID, DTUPayUser merchantInfo) {
		appliedEvents.add( (PaymentEvent)new TransactionMerchantInfoAdded(transactionID,merchantInfo));
        applyEvents(appliedEvents.stream());

    }
    public void addCustomerInfo(String transactionID, DTUPayUser customerInfo) {
        appliedEvents.add( (PaymentEvent)new TransactionCustomerInfoAdded(transactionID,customerInfo));
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

	private void apply(TransactionMerchantInfoAdded event) {
		transactionID = event.getTransactionID();
		merchantBankID = event.getMerchantInfo().getBankId().getBankAccountId();
		merchant = event.getMerchantInfo().getPerson();
		merchantID= event.getMerchantInfo().getUniqueId();
	}

	private void apply(TransactionCustomerInfoAdded event) {
		transactionID = event.getTransactionID();
		customerBankID = event.getCustomerInfo().getBankId().getBankAccountId();
		customer = event.getCustomerInfo().getPerson();
		customerID = event.getCustomerInfo().getUniqueId();
	}

	public void clearAppliedEvents() {
		appliedEvents.clear();
	}
	public boolean complete(){
		return customerBankID != null && merchantBankID != null && amount!= null;
	}

}
