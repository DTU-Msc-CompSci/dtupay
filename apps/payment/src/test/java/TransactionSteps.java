import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

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

import org.acme.aggregate.*;
import org.acme.repositories.PaymentRepository;
import org.acme.repositories.ReadModelRepository;
import org.acme.service.TransactionService;

import static org.mockito.Mockito.*;


public class TransactionSteps {
    private MessageQueue q = mock(MessageQueue.class);
    //private AccountService service = mock ( AccountService.class , withSettings().useConstructor(q));
    private ReadModelRepository readmodel = new ReadModelRepository(q);
    private TransactionService service = new TransactionService(q, new PaymentRepository(q),readmodel );

    private BankService bankService =  new BankServiceService().getBankServicePort();
    private DTUPayUser customer;
    private DTUPayUser merchant;
    private String transactionID ="transactionid";
    private String customerToken ="customertoken";
    private String merchantID="merchantid";
    private CompletableFuture<Boolean> done1 = new CompletableFuture();
    private CompletableFuture<Boolean> done2 = new CompletableFuture();
    private CompletableFuture<Boolean> done3 = new CompletableFuture();
    private int amount= 100;


    @Before
    public void beforeStep() {
        User cost = new User();

        cost.setFirstName("Johneefrfvrfvfgbdfffvervvrdrfvb");
        cost.setLastName("Rambeedfrfvrfrrfvergbfvvffvgfrvo");
        cost.setCprNumber("12erddfffvvgbrfvferfvvrffgv3123");
        customer = new DTUPayUser();
        customer.setPerson( new Person(cost.getFirstName(),cost.getLastName(),cost.getCprNumber()));
        customer.setUniqueId("uniquecustomerId");
        User mer = new User();
        mer.setFirstName("Joedrdfeffvrfvfgbvrvfffvgrvhn");
        mer.setLastName("Wicedfdfefvfvfvrgbrfvrffvgrvk");
        mer.setCprNumber("32erddffeffvfvrgbfvrrfvfgv1321");
        merchant = new DTUPayUser();
        merchant.setPerson( new Person(mer.getFirstName(),mer.getLastName(),mer.getCprNumber()));
        merchant.setUniqueId("uniquemerchantId");
        try {
            customer.setBankId(new BankId(bankService.createAccountWithBalance(cost, BigDecimal.valueOf(1000))));
            merchant.setBankId(new BankId(bankService.createAccountWithBalance(mer, BigDecimal.valueOf(1000))));

        } catch (BankServiceException_Exception e) {
            throw new RuntimeException(e);
        }
    }
    @After
    public void afterStep() {
        try {
            bankService.retireAccount(customer.getBankId().getBankAccountId());
            bankService.retireAccount(merchant.getBankId().getBankAccountId());
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

        var event2  = new Event("MerchantInfoProvided", new Object[] {transactionID, merchant});
        var event3  = new Event("CustomerInfoProvided",new Object[] {transactionID, customer});
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
    }
    @Then("money is only transferred once")
    public void money_is_only_transferred_once() throws BankServiceException_Exception {
        Event transactionCompletedEvent = new Event("TransactionCompleted", new Object[] { transactionID, "completed" });
        verify(q,times(1)).publish(transactionCompletedEvent);

    }

    @Given("{string} Event")
    public void event(String string) {
        // Write code here that turns the phrase above into concrete actions
        Event transactionCustomerInfoEvent = new Event(string, new Object[] { transactionID, customer });
        readmodel.handleTransactionCustomerInfoAdded(transactionCustomerInfoEvent);

    }

    @When("info is added to the view")
    public void info_is_added_to_the_view() {
        // Write code here that turns the phrase above into concrete actions
    }

    @Then("the repo contains customer information")
    public void the_repo_contains_customer_information() {
        // Write code here that turns the phrase above into concrete actions
        var a = readmodel.getAllPayments();
        var b = a;

    }

    @When("invalid transaction data is sent to the service")
    public void invalidTransactionDataIsSentToTheService() {
        Token token =  new Token(customerToken);
        var Transaction =  new Transaction();
        Transaction.setAmount(2000);
        Transaction.setCustomerToken(token);
        Transaction.setMerchantId(merchantID);

        var event1 = new Event( "TransactionRequested", new Object[] {transactionID, Transaction});

        var event2  = new Event("MerchantInfoProvided", new Object[] {transactionID, merchant});
        var event3  = new Event("CustomerInfoProvided",new Object[] {transactionID, customer});
        service.handlePayment(event1);
        service.handlePayment(event2);
        service.handlePayment(event3);
    }

    @Then("a transactionFailed event is published")
    public void aTransactionFailedEventIsPublished() {
        Event transactionFailedEvent = new Event("TransactionFailed", new Object[] { transactionID, "Transaction failed" });
        verify(q, times(1)).publish(transactionFailedEvent);
    }

}