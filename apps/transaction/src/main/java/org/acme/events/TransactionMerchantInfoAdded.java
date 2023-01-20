package org.acme.events;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.acme.aggregate.DTUPayUser;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TransactionMerchantInfoAdded extends PaymentEvent {
    private static final long serialVersionUID = 9023222281284906610L;
    private String transactionID;
    private DTUPayUser merchantInfo;

}
