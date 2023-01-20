import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException;
import dtu.ws.fastmoney.BankServiceException_Exception;
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
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;


public class TransactionSteps {
    private final MessageQueue mockQueue = mock(MessageQueue.class);
    private final ReadModelRepository readmodel = new ReadModelRepository(mockQueue);
    BankService bankService = mock(BankService.class);
    private final TransactionService service = new TransactionService(mockQueue, bankService, new PaymentRepository(mockQueue), readmodel);

    private DTUPayUser customer;
    private DTUPayUser customer2;
    private DTUPayUser merchant;
    private final String transactionID = "transactionid";
    private final String transactionID2 = "transactionid2";
    private final String customerToken = "customertoken";
    private final String customerToken2 = "customertoken2";
    private final String merchantID = "merchantid";
    private final CompletableFuture<Boolean> done1 = new CompletableFuture<>();
    private final CompletableFuture<Boolean> done2 = new CompletableFuture<>();
    private final CompletableFuture<Boolean> done3 = new CompletableFuture<>();
    private final int amount = 100;
    private final int amount2 = 200;


    @Before
    public void beforeStep() {
        User cost = new User();

        cost.setFirstName("John");
        cost.setLastName("Doe");
        cost.setCprNumber("123456-7890");
        customer = new DTUPayUser();
        customer.setPerson(new Person(cost.getFirstName(), cost.getLastName(), cost.getCprNumber()));
        customer.setUniqueId("uniquecustomerId");

        User cost2 = new User();

        cost2.setFirstName("Bob");
        cost2.setLastName("Doe");
        cost2.setCprNumber("547321-7890");
        customer2 = new DTUPayUser();
        customer2.setPerson(new Person(cost2.getFirstName(), cost2.getLastName(), cost2.getCprNumber()));
        customer2.setUniqueId("uniquecustomerId2");


        User mer = new User();
        mer.setFirstName("Joe");
        mer.setLastName("Wice");
        mer.setCprNumber("098765-4321");
        merchant = new DTUPayUser();
        merchant.setPerson(new Person(mer.getFirstName(), mer.getLastName(), mer.getCprNumber()));
        merchant.setUniqueId("uniquemerchantId");
        customer.setBankId(new BankId("customerBankId"));
        customer2.setBankId(new BankId("customerBankId2"));
        merchant.setBankId(new BankId("merchantBankId"));
    }

    @After
    public void afterStep() {
    }

    @Given("a concurrent {string} Event and a {string} Even and a {string} Event")
    public void a_concurrent_event_and_a_even_and_a_event(String string, String string2, String string3) {
        Token token = new Token(customerToken);
        var Transaction = new Transaction();
        Transaction.setAmount(amount);
        Transaction.setCustomerToken(token);
        Transaction.setMerchantId(merchantID);

        var event1 = new Event("TransactionRequested", new Object[]{transactionID, Transaction});

        var event2 = new Event("MerchantInfoProvided", new Object[]{transactionID, merchant});
        var event3 = new Event("CustomerInfoProvided", new Object[]{transactionID, customer});
        new Thread(() -> {
            service.handlePayment(event1);
            done1.complete(true);
        }).start();
        new Thread(() -> {
            service.handlePayment(event2);
            done2.complete(true);
        }).start();

        new Thread(() -> {
            service.handlePayment(event3);
            done3.complete(true);
        }).start();
        done1.join();
        done2.join();
        done3.join();

    }

    @When("transaction is initiated")
    public void transaction_is_initiated() {
        try {
            verify(bankService, times(1)).transferMoneyFromTo(customer.getBankId().getBankAccountId(), merchant.getBankId().getBankAccountId(), BigDecimal.valueOf(amount), "DTU Pay transaction");
        } catch (BankServiceException_Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Then("money is only transferred once")
    public void money_is_only_transferred_once() throws BankServiceException_Exception {
        Event transactionCompletedEvent = new Event("TransactionCompleted", new Object[]{transactionID, "Success"});
        verify(mockQueue, times(1)).publish(transactionCompletedEvent);

    }

    @Given("{string} Event")
    public void event(String string) {
        Event transactionCustomerInfoEvent = new Event(string, new Object[]{transactionID, customer});
        readmodel.handleTransactionCustomerInfoAdded(transactionCustomerInfoEvent);

    }

    @When("info is added to the view")
    public void info_is_added_to_the_view() {
    }

    @Then("the repo contains customer information")
    public void the_repo_contains_customer_information() {
        var a = readmodel.getAllPayments();
        var b = a;

    }

    @When("invalid transaction data is sent to the service")
    public void invalidTransactionDataIsSentToTheService() throws BankServiceException_Exception {
        Mockito.doThrow(new BankServiceException_Exception("Failed", new BankServiceException())).when(bankService).transferMoneyFromTo(customer.getBankId().getBankAccountId(), merchant.getBankId().getBankAccountId(), BigDecimal.valueOf(-1), "DTU Pay transaction");
        Token token = new Token(customerToken);
        var Transaction = new Transaction();
        Transaction.setAmount(-1);
        Transaction.setCustomerToken(token);
        Transaction.setMerchantId(merchantID);

        var event1 = new Event("TransactionRequested", new Object[]{"123", Transaction});

        var event2 = new Event("MerchantInfoProvided", new Object[]{"123", merchant});
        var event3 = new Event("CustomerInfoProvided", new Object[]{"123", customer});
        service.handlePayment(event1);
        service.handlePayment(event2);
        service.handlePayment(event3);
    }

    @Then("a transactionFailed event is published")
    public void aTransactionFailedEventIsPublished() {
        Event transactionFailedEvent = new Event("TransactionFailed", new Object[]{"123", "Transaction failed"});
        verify(mockQueue, times(1)).publish(transactionFailedEvent);
    }

    @When("the merchant report is requested")
    public void theMerchantReportIsRequested() {
        Event merchantReportRequestedEvent = new Event("MerchantReportRequested", new Object[]{transactionID, merchantID});
        service.handleMerchantReportRequested(merchantReportRequestedEvent);
    }


    @Then("the correct report is sent to the merchant")
    public void theCorrectReportIsSentToTheMerchant() {
        var report = readmodel.getMerchantPayment(merchantID);
        ReportUserResponse resp = new ReportUserResponse();
        resp.setReports(report);
        Event merchantReportEvent = new Event("MerchantReportCreated", new Object[] { transactionID, resp });
        verify(mockQueue, times(1)).publish(merchantReportEvent);
    }

    @When("the customer report is requested")
    public void theCustomerReportIsRequested() {
        Event customerReportRequestedEvent = new Event("CustomerReportRequested", new Object[]{transactionID, customer.getUniqueId()});
        service.handleCustomerReportRequested(customerReportRequestedEvent);
    }


    @Then("the correct report is sent to the customer")
    public void theCorrectReportIsSentToTheCustomer() {
        var report = readmodel.getCustomerPayment(merchantID);
        ReportUserResponse resp = new ReportUserResponse();
        resp.setReports(report);
        Event customerReportEvent = new Event("CustomerReportCreated", new Object[] { transactionID, resp });
        verify(mockQueue, times(1)).publish(customerReportEvent);
    }

    @When("the manager report is requested")
    public void theManagerReportIsRequested() {
        Event managerReportRequestedEvent = new Event("ManagerReportRequested", new Object[]{transactionID});
        service.handleManagerReportRequested(managerReportRequestedEvent);
    }


    @Then("the correct report is sent to the manager")
    public void theCorrectReportIsSentToTheManager() {
        var report = readmodel.getAllPayments();
        ReportManagerResponse resp = new ReportManagerResponse();
        resp.setReports(report);
        Event managerReportEvent = new Event("ManagerReportCreated", new Object[] { transactionID, resp });
        verify(mockQueue, times(1)).publish(managerReportEvent);
    }

    @When("the service receives events for two transactions interleaved")
    public void theServiceReceivesEventsForTwoTransactionsInterleaved() {
        Token token = new Token(customerToken);
        var Transaction = new Transaction();
        Transaction.setAmount(amount);
        Transaction.setCustomerToken(token);
        Transaction.setMerchantId(merchantID);

        Token token2 = new Token(customerToken2);
        var transaction2 = new Transaction();
        transaction2.setAmount(amount2);
        transaction2.setCustomerToken(token2);
        transaction2.setMerchantId(merchantID);


        var event1 = new Event("TransactionRequested", new Object[]{transactionID, Transaction});

        var event2 = new Event("MerchantInfoProvided", new Object[]{transactionID, merchant});
        var event3 = new Event("CustomerInfoProvided", new Object[]{transactionID, customer});

        var event4 = new Event("TransactionRequested", new Object[]{transactionID2, transaction2});
        var event5 = new Event("MerchantInfoProvided", new Object[]{transactionID2, merchant});
        var event6 = new Event("CustomerInfoProvided", new Object[]{transactionID2, customer2});

        service.handlePayment(event1);
        service.handlePayment(event4);
        service.handlePayment(event5);
        service.handlePayment(event3);
        service.handlePayment(event2);
        service.handlePayment(event6);
    }

    @Then("the transferMoneyToFrom method of the bank is called twice with correct values")
    public void theTransferMoneyToFromMethodOfTheBankIsCalledTwiceWithCorrectValues() {
        try {
            verify(bankService).transferMoneyFromTo(customer.getBankId().getBankAccountId(), merchant.getBankId().getBankAccountId(), BigDecimal.valueOf(amount), "DTU Pay transaction");
            verify(bankService).transferMoneyFromTo(customer2.getBankId().getBankAccountId(), merchant.getBankId().getBankAccountId(), BigDecimal.valueOf(amount2), "DTU Pay transaction");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Then("two transactionCompleted events are sent with correct values")
    public void twoTransactionCompletedEventsAreSentWithCorrectValues() {
        Event transactionCompletedEvent = new Event("TransactionCompleted", new Object[]{transactionID, "Success"});
        Event transactionCompletedEvent2 = new Event("TransactionCompleted", new Object[]{transactionID2, "Success"});
        verify(mockQueue).publish(transactionCompletedEvent);
        verify(mockQueue).publish(transactionCompletedEvent2);
    }
}
