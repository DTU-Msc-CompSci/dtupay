package org.acme.events;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.acme.aggregate.Payment;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TransactionMerchantBankIDAdded extends PaymentEvent {

		// Might need to change the number depending on the User being referenced
	private static final long serialVersionUID = 9023222281284906610L;
	private String transactionID;
    private String merchantBankID;
    
}
