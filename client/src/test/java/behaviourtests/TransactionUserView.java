package behaviourtests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionUserView implements Serializable {
    private static final long serialVersionUID = 9023222281284906610L;

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

    @Override
    public String toString() {
        return String.format("Transaction id: %s", transactionId);
    }
}
