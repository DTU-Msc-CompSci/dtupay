package org.acme;

import java.math.BigDecimal;

public class TransactionData {

    private String customerBankId;

    private String merchantBankId;

    private BigDecimal amount;

    public void setCustomerBankId(String customerBankId) {
        this.customerBankId = customerBankId;
    }

    public String getCustomerBankId() {
        return customerBankId;
    }

    public void setMerchantBankId(String merchantBankId) {
        this.merchantBankId = merchantBankId;
    }

    public String getMerchantBankId() {
        return merchantBankId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public boolean isComplete(){
        return customerBankId != null && merchantBankId != null && amount != null;
    }
}

