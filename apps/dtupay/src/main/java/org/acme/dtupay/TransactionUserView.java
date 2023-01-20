package org.acme.dtupay;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

//TODO This xml thing
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionUserView {
    private String customerToken;

    private String merchantId;
    private DTUPayUser merchant;

    private BigDecimal amount;

    private String transactionId;
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TransactionUserView)) {
            return false;
        }
        var c = (TransactionUserView) o;
        return customerToken != null && customerToken.equals(c.getCustomerToken()) &&
                merchantId != null && merchantId.equals(c.getMerchantId()) &&
                amount == (c.getAmount());
    }

    @Override
    public int hashCode() {
        return  transactionId == null ? 0 : transactionId.hashCode();
    }



}
