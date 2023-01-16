import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

//TODO This xml thing
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction implements Serializable {
    // Might need to change the number depending on the User being referenced
    private static final long serialVersionUID = 9023222281284906610L;
    ;
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
        return  transactionId == null ? 0 : transactionId.hashCode();
    }

    @Override
    public String toString() {
        // uniqueId could potentially be null
        return String.format("Transaction id: %s", transactionId);
    }
}
