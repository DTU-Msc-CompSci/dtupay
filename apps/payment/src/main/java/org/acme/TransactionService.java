package org.acme;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankServiceService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TransactionService {

    private List<Transaction> transactions= new ArrayList<Transaction>();
    private BankService bankService =  new BankServiceService().getBankServicePort();


    public String getGreetings() {
        return "Hello RESTEasy";
    }


    public void initiateTransaction(Transaction transaction) {
        // Right now bankId == DTUPayID but this should change when we add registration service
        //TODO fetch the bank id from a registration service
        var customerBankAccountID = transaction.getCid();
        var merchantBankAccountID = transaction.getMid();
        try {
            bankService.transferMoneyFromTo(customerBankAccountID,merchantBankAccountID,  BigDecimal.valueOf((transaction.getAmount())), "Empty description returns an error!! So, this description is fine for now");
            transactions.add(transaction);

        } catch (BankServiceException_Exception e) {
            throw new RuntimeException(e);
        }


    }

}
