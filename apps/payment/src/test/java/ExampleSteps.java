import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankServiceService;
import dtu.ws.fastmoney.User;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;

import org.acme.*;
import org.acme.aggregate.Payment;
import org.acme.aggregate.Token;
import org.acme.aggregate.Transaction;
import org.acme.repositories.PaymentRepository;
import org.acme.repositories.ReadModelRepository;
import org.acme.service.TransactionService;

import java.util.*;
import static org.mockito.Mockito.*;


public class ExampleSteps {
    private MessageQueue q = mock(MessageQueue.class);
    //private AccountService service = mock ( AccountService.class , withSettings().useConstructor(q));
    private TransactionService service = new TransactionService(q, new PaymentRepository(q), new ReadModelRepository(q));

    private BankService bankService =  new BankServiceService().getBankServicePort();
    private String customerBankAccountId;
    private String merchantBankAccountId;
    private String transactionID ="transactionid";
    private String customerToken ="customertoken";
    private String merchantID="merchantid";
    private CompletableFuture<Boolean> done1 = new CompletableFuture();
    private CompletableFuture<Boolean> done2 = new CompletableFuture();
    private CompletableFuture<Boolean> done3 = new CompletableFuture();
    private int amount= 100;


    @Given("A naive scenario")
    public void naiveScenario() {

        //tests for debugging need to be removed for final hand in
//         MessageQueue q = mock(MessageQueue.class);
//         //mo
//         String customerBankID="customerBankid";
//         String merchantBankID="merchantbankid";
//
//        PaymentRepository pr = new PaymentRepository(q);
//        Payment payment = pr.getById(transactionID);
//        payment.addCustomerBankID(transactionID, customerBankID);
//        payment.addMerchantBankID(transactionID, merchantBankID);
//        payment.create(transactionID, customerToken,merchantID,amount);
//        pr.save(payment);
//        var payment2 = pr.getById(transactionID);
//        var paymentsdfw = payment2;





    }
    @Before
    public void beforeStep() {
        User cost = new User();
        cost.setFirstName("Johneefrrfdfffvervvrdrfvb");
        cost.setLastName("Rambeedfrrfrrerfvvffvgfrvo");
        cost.setCprNumber("12erddffvrferfvvrffgv3123");

        User mer = new User();
        mer.setFirstName("Joedrdfefrfvrvfffvgrvhn");
        mer.setLastName("Wicedfdfefvrrfvrffvgrvk");
        mer.setCprNumber("32erddffefrfvrrfvfgv1321");
        try {
            customerBankAccountId = bankService.createAccountWithBalance(cost, BigDecimal.valueOf(1000));
            merchantBankAccountId = bankService.createAccountWithBalance(mer, BigDecimal.valueOf(1000));
        } catch (BankServiceException_Exception e) {
            throw new RuntimeException(e);
        }
    }
    @After
    public void afterStep() {
        try {
            bankService.retireAccount(customerBankAccountId);
            bankService.retireAccount(merchantBankAccountId);
        } catch (BankServiceException_Exception e) {
        }
    }

    @Given("a concurrent {string} Event and a {string} Even and a {string} Event")
    public void a_concurrent_event_and_a_even_and_a_event(String string, String string2, String string3) {
        // Write code here that turns the phrase above into concrete actions
        Token token =  new Token(customerToken);
        var Transaction =  new Transaction();
        Transaction.setAmount(amount);
        Transaction.setCustomerToken(token);
        Transaction.setMerchantId(merchantID);

        var event1 = new Event( "TransactionRequested", new Object[] {transactionID, Transaction});

        var event2  = new Event("MerchantInfoProvided", new Object[] {transactionID, merchantBankAccountId});
        var event3  = new Event("CustomerInfoProvided",new Object[] {transactionID, customerBankAccountId});
        new Thread(() -> { service.handlePayment(event1); done1.complete(true); }).start();
        new Thread(() -> { service.handlePayment(event2); done2.complete(true);}).start();

        new Thread(() -> { service.handlePayment(event3); done3.complete(true);}).start();
        done1.join();
        done2.join();
        done3.join();

    }
    @When("transaction is initiated")
    public void transaction_is_initiated() {
        // Write code here that turns the phrase above into concrete actions
        Event transactionCompletedEvent = new Event("TransactionCompleted", new Object[] { "completed" });
    }
    @Then("money is only transferred once")
    public void money_is_only_transferred_once() throws BankServiceException_Exception {
        Event transactionCompletedEvent = new Event("TransactionCompleted", new Object[] { "completed" });
        verify(q,times(1)).publish(transactionCompletedEvent);

    }
}
