package behaviourtests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

//TODO This xml thing
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionManagerView implements Serializable {
    // Might need to change the number depending on the User being referenced
    private static final long serialVersionUID = 9023222281284906610L;

    private String customerToken;
    private BigDecimal amount;
    private String merchantId;
    private String customerId;

    private DTUPayUser customer;
    private DTUPayUser merchant;


    private String transactionId;
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TransactionManagerView)) {
            return false;
        }
        var c = (TransactionManagerView) o;
        return customerToken != null && customerToken.equals(c.getCustomerToken()) &&
                merchant != null && merchant.equals(c.getMerchant()) &&
                merchantId != null && merchantId.equals(c.getMerchantId()) &&
                transactionId != null && transactionId.equals(c.getTransactionId()) &&

                customer != null && customer.equals(c.getCustomer()) &&

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

    public TransactionUserView toUserView() {
        return new TransactionUserView(customerToken,merchantId,merchant,amount,transactionId);
    }
}
