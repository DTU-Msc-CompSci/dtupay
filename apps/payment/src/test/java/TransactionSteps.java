import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import dtu.ws.fastmoney.BankService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;

import org.acme.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class TransactionSteps {

    MessageQueue queue = mock(MessageQueue.class);
    BankService bank = mock(BankService.class);
    TransactionService transactionService = new TransactionService(queue, bank);
    String correlationId = UUID.randomUUID().toString();
    String correlationId2 = UUID.randomUUID().toString();
    @When("the service receives a TransactionRequested event")
    public void theServiceReceivesATransactionRequestedEvent() {
        var transaction = new Transaction(null, null, 100, correlationId);
        var event = new Event("TransactionRequested", new Object[] { correlationId, transaction });
        transactionService.handleTransactionEvent(event);
    }

    @When("a CustomerInfoProvided event with the same payment id is received")
    public void aCustomerInfoProvidedEventWithTheSamePaymentIdIsReceived() {
        var event = new Event("CustomerInfoProvided", new Object[] { correlationId, "customer" });
        transactionService.handleTransactionEvent(event);

    }

    @When("a MerchantInfoProvided event with the same payment id is received")
    public void aMerchantInfoProvidedEventWithTheSamePaymentIdIsReceived() {
        var event = new Event("MerchantInfoProvided", new Object[] { correlationId, "merchant" });
        transactionService.handleTransactionEvent(event);
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
        verify(queue).publish(new Event("TransactionCompleted", new Object[] {correlationId}));
    }


    @When("the service receives events for two transactions interleaved")
    public void theServiceReceivesEventsForTwoTransactionsInterleaved() {
        var transaction = new Transaction(null, null, 100, correlationId);
        var event = new Event("TransactionRequested", new Object[] { correlationId, transaction});

        var transaction2 = new Transaction(null, null, 100, correlationId2);
        var event2 = new Event("TransactionRequested", new Object[] { correlationId2, transaction2});

        var event3 = new Event("CustomerInfoProvided", new Object[] { correlationId, "customer" });

        var event4 = new Event("CustomerInfoProvided", new Object[] { correlationId2, "customer2"});

        var event5 = new Event("MerchantInfoProvided", new Object[] { correlationId, "merchant"});

        var event6 = new Event("MerchantInfoProvided", new Object[] { correlationId2, "merchant"});

        transactionService.handleTransactionEvent(event3);
        transactionService.handleTransactionEvent(event5);
        transactionService.handleTransactionEvent(event);
        transactionService.handleTransactionEvent(event4);
        transactionService.handleTransactionEvent(event6);
        transactionService.handleTransactionEvent(event2);
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
        verify(queue).publish(new Event("TransactionCompleted", new Object[] {correlationId}));
        verify(queue).publish(new Event("TransactionCompleted", new Object[] {correlationId2}));
    }

    @When("the service receives all three events for a transaction simultaneously")
    public void theServiceReceivesAllThreeEventsForATransactionSimultaneously() {
        var transaction = new Transaction(null, null, 100, correlationId);
        var event = new Event("TransactionRequested", new Object[] { correlationId, transaction});

        var event2 = new Event("CustomerInfoProvided", new Object[] { correlationId, "customer" });

        var event3 = new Event("MerchantInfoProvided", new Object[] { correlationId, "merchant"});

        var done = new CompletableFuture<Boolean>();
        var done2 = new CompletableFuture<Boolean>();
        var done3 = new CompletableFuture<Boolean>();

        new Thread(() -> {transactionService.handleTransactionEvent(event3); done3.complete(true);}).start();
        new Thread(() -> {transactionService.handleTransactionEvent(event); done.complete(true);}).start();
        new Thread(() -> {transactionService.handleTransactionEvent(event2); done2.complete(true);}).start();

        done.join();
        done2.join();
        done3.join();
    }

    @Then("the transferMoneyToFrom method of the bank is only called once")
    public void theTransferMoneyToFromMethodOfTheBankIsOnlyCalledOnce() {
        try {
            verify(bank, times(1)).transferMoneyFromTo("customer", "merchant", new BigDecimal(100), "DTU Pay transaction");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @And("a transactionCompleted event is only sent once")
    public void aTransactionCompletedEventIsOnlySentOnce() {
        verify(queue, times(1)).publish(new Event("TransactionCompleted", new Object[] {correlationId}));
    }
}
