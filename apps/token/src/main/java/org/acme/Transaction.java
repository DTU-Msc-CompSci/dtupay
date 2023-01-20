package org.acme;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction implements Serializable {
    private static final long serialVersionUID = 9023222281284906610L;

    private Token customerToken;

    private String merchantId;

    private int amount;

    String transactionId;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Transaction)) {
            return false;
        }
        var c = (Transaction) o;
        return customerToken != null && customerToken.equals(c.getCustomerToken()) &&
                merchantId != null && merchantId.equals(c.getMerchantId()) &&
                amount == (c.getAmount());
    }

    @Override
    public int hashCode() {
        return transactionId == null ? 0 : transactionId.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Transaction id: %s", transactionId);
    }
}
