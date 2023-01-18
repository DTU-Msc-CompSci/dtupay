package org.acme.events;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.math.BigDecimal;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TransactionCreated extends PaymentEvent {

	private static final long serialVersionUID = -1599019626118724482L;
    private String transactionID;
    private String customerToken ;
    private String merchantID;
    private BigDecimal amount;
}
