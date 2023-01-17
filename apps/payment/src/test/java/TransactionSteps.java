import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import dtu.ws.fastmoney.BankService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;

import org.acme.*;
import java.util.*;

public class TransactionSteps {

    MessageQueue queue = mock(MessageQueue.class);
    BankService bank = mock(BankService.class);
    TransactionService transactionService = new TransactionService(queue, bank);
    UUID transactionId = UUID.randomUUID();
    UUID transactionId2 = UUID.randomUUID();
    @When("the service receives a TransactionRequested event")
    public void theServiceReceivesATransactionRequestedEvent() {
        var transaction = new Transaction(null, null, 100, transactionId);
        var event = new Event("TransactionRequested", new Object[] {transaction});
        transactionService.handleTransactionRequested(event);
    }

    @When("a CustomerInfoProvided event with the same payment id is received")
    public void aCustomerInfoProvidedEventWithTheSamePaymentIdIsReceived() {
        var event = new Event("CustomerInfoProvided", new Object[] {"customer", transactionId});
        transactionService.handleCustomerInfoProvided(event);

    }

    @When("a MerchantInfoProvided event with the same payment id is received")
    public void aMerchantInfoProvidedEventWithTheSamePaymentIdIsReceived() {
        var event = new Event("MerchantInfoProvided", new Object[] {"merchant", transactionId});
        transactionService.handleMerchantInfoProvided(event);
    }
    @Then("the transferMoneyToFrom method of the bank is called")
    public void theTransferMoneyToFromMethodOfTheBankIsCalled() {
        try{
            verify(bank).transferMoneyFromTo("customer", "merchant", new BigDecimal(100), "DTU Pay transaction");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Then("a transactionCompleted event is sent with the same payment id")
    public void aTransactionCompletedEventIsSentWithTheSamePaymentId(){
        verify(queue).publish(new Event("TransactionCompleted", new Object[] {transactionId}));
    }


    @When("the service receives events for two transactions interleaved")
    public void theServiceReceivesEventsForTwoTransactionsInterleaved() {
        var transaction = new Transaction(null, null, 100, transactionId);
        var event = new Event("TransactionRequested", new Object[] {transaction});

        var transaction2 = new Transaction(null, null, 100, transactionId2);
        var event2 = new Event("TransactionRequested", new Object[] {transaction2});

        var event3 = new Event("CustomerInfoProvided", new Object[] {"customer", transactionId});

        var event4 = new Event("CustomerInfoProvided", new Object[] {"customer2", transactionId2});

        var event5 = new Event("MerchantInfoProvided", new Object[] {"merchant", transactionId});

        var event6 = new Event("MerchantInfoProvided", new Object[] {"merchant", transactionId2});

        transactionService.handleCustomerInfoProvided(event3);
        transactionService.handleMerchantInfoProvided(event5);
        transactionService.handleTransactionRequested(event);
        transactionService.handleCustomerInfoProvided(event4);
        transactionService.handleMerchantInfoProvided(event6);
        transactionService.handleTransactionRequested(event2);
    }

    @Then("the transferMoneyToFrom method of the bank is called twice with correct values")
    public void theTransferMoneyToFromMethodOfTheBankIsCalledTwiceWithCorrectValues() {
        try {
            verify(bank).transferMoneyFromTo("customer", "merchant", new BigDecimal(100), "DTU Pay transaction");
            verify(bank).transferMoneyFromTo("customer2", "merchant", new BigDecimal(100), "DTU Pay transaction");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Then("two transactionCompleted events are sent with correct values")
    public void twoTransactionCompletedEventsAreSentWithCorrectValues() {
        verify(queue).publish(new Event("TransactionCompleted", new Object[] {transactionId}));
        verify(queue).publish(new Event("TransactionCompleted", new Object[] {transactionId2}));
    }
}
