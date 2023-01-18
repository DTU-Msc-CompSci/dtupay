package org.acme.events;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.acme.aggregate.DTUPayUser;
import org.acme.aggregate.Payment;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TransactionCustomerInfoAdded extends PaymentEvent {

	private static final long serialVersionUID = 3699730769270260597L;
	private String transactionID;
    private DTUPayUser customerInfo;
    
}
