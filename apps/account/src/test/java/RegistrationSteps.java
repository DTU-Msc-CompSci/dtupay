import static org.junit.Assert.assertFalse;
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
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankServiceService;

import dtu.ws.fastmoney.User;
import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;

import messaging.implementations.RabbitMqQueue;
import org.acme.*;
import java.util.*;

public class RegistrationSteps {

    private CompletableFuture<Event> publishedEvent = new CompletableFuture<>();

    private MessageQueue q = new MessageQueue() {

        @Override
        public void publish(Event event) {
            publishedEvent.complete(event);
        }

        @Override
        public void addHandler(String eventType, Consumer<Event> handler) {
        }

    };

    private MessageQueue mockQueue = mock(MessageQueue.class);

    private AccountService service = new AccountService(q);
    private AccountService accountServiceMockQueue = new AccountService(mockQueue);
    private CompletableFuture<DTUPayUser> registeredUser = new CompletableFuture<>();
    private Person person;

    private MessageQueue q = mock(MessageQueue.class);
    //private AccountService service = mock ( AccountService.class , withSettings().useConstructor(q));
    private AccountService service = new AccountService(q);
    private DTUPayUser customer = new DTUPayUser();
    private DTUPayUser merchant = new DTUPayUser();

    private BankService bankService =  new BankServiceService().getBankServicePort();
    private String customerBankAccountId;
    private String merchantBankAccountId;


    public RegistrationSteps() {
    }

    @Before
    public void beforeStep() {
        User cost = new User();
        cost.setFirstName("John");
        cost.setLastName("Rambo");
        cost.setCprNumber("123123");

        User mer = new User();
        mer.setFirstName("John");
        mer.setLastName("Wick");
        mer.setCprNumber("321321");
        try {
            customerBankAccountId = bankService.createAccountWithBalance(cost, BigDecimal.valueOf(10));
            merchantBankAccountId = bankService.createAccountWithBalance(mer, BigDecimal.valueOf(10));
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

    @Given("There is a customer with empty id")
    public void thereIsACustomerWithEmptyId() {
        Person person = new Person("John", "Rambo", "123123");
        customer.setPerson(person);
        customer.setBankId(new BankId(customerBankAccountId));
        assertNull(customer.getUniqueId());
    }

    @Given("There is a merchant with empty id")
    public void thereIsAMerchantWithEmptyId() {
        Person person = new Person("John", "Wick", "321321");
        merchant.setPerson(person);
        merchant.setBankId(new BankId(merchantBankAccountId));
        assertNull(merchant.getUniqueId());
    }

    @When("the service receives a {word}AccountCreationRequested event")
    public void theServiceReceivesUserAccountCreationRequestedEvent(String userType){
        boolean flag = false;
        if(userType.equals("Customer")) {
            var event = new Event("CustomerAccountCreationRequested", new Object[]{customer});
            customer.setUniqueId(service.handleCustomerAccountCreationRequested(event));
            flag = true;
        } else if(userType.equals("Merchant")) {
            var event = new Event("MerchantAccountCreationRequested", new Object[]{merchant});
            merchant.setUniqueId(service.handleMerchantAccountCreationRequested(event));
            flag = true;
        }
        assertTrue(flag);
    }

    @Then("a {word}AccountCreated event is published")
    public void aUserAccountCreatedPublished(String userType) {
        Event event = null;
        if(userType.equals("Customer")) {
            event = new Event("CustomerAccountCreated", new Object[]{customer});
        } else if(userType.equals("Merchant")) {
            event = new Event("MerchantAccountCreated", new Object[]{merchant});
        }
        verify(q).publish(event);
    }

    @When("the event is started")
    public void theEventIsStarted() {
        new Thread(() -> {
            accountServiceMockQueue.addCustomer(customer);
        }).start();
    }


    @Then("the event is being processed and the correlation ID is present in the CustomerAccountCreationRequested event")
    public void theEventIsBeingProcessedAndTheCorrelationIDIsPresentInTheCustomerAccountCreationRequestedEvent() {
        mockQueue.addHandler("CustomerAccountCreationRequested", e -> {
            var eventCorrelationId = e.getArgument(0, String.class);
            var isProcessing = true;
            assertTrue(isProcessing);
            assertNotNull(eventCorrelationId);
        });
    @When("the service receives a {word}AccountDeRegistrationRequested event")
    public void theServiceReceivesUserAccountDeRegistrationRequested(String userType) {
        if (userType.equals("Customer")) {
            Event event = new Event("CostumerAccountCreationFailed", new Object[]{customer.getUniqueId()});
            service.handleCustomerAccountDeRegistrationRequested(event);
        } else if(userType.equals("Merchant")) {
            Event event = new Event("MerchantAccountCreationFailed", new Object[]{merchant.getUniqueId()});
            service.handleMerchantAccountDeRegistrationRequested(event);
        }
    }

    @Then("a {word}AccountDeRegistrationCompleted event is published")
    public void aUserAccountDeRegistrationCompletedPublished(String userType) {
        Event event = null;
        if (userType.equals("Customer")) {
            event = new Event("CustomerAccountDeRegistrationCompleted");
        } else if(userType.equals("Merchant")) {
            event = new Event("MerchantAccountDeRegistrationCompleted");
        }
        verify(q).publish(event);
    }

    @Then("a {word}AccountCreationFailed event is published")
    public void aUserAccountCreationFailedEventPublished(String userType) {
        Event event = null;
        if (userType.equals("Customer")) {
            event = new Event("CustomerAccountCreationFailed");
        } else if(userType.equals("Merchant")) {
            event = new Event("MerchantAccountCreationFailed");
        }
        verify(q).publish(event);
    }

    @And("the {string} should exist in the database")
    public void userExists(String userType) {
        boolean exist = false;
        if(userType.equals("customer")) {
            exist = service.doesCostumerExist(customer.getBankId().getBankAccountId());
        } else if(userType.equals("merchant")) {
            exist = service.doesMerchantExist(merchant.getBankId().getBankAccountId());
        }
        assertTrue(exist);
    }

    @And("the {string} should not exist in the database")
    public void userDoesNotExist(String userType) {
        boolean exist = true;
        if(userType.equals("customer")) {
            exist = service.doesCostumerExist(customer.getBankId().getBankAccountId());
        } else if(userType.equals("merchant")) {
            exist = service.doesMerchantExist(merchant.getBankId().getBankAccountId());
        }
        assertFalse(exist);
    }

    @Given("There is a {string} with fake bankId")
    public void thereIsACustomerWithFakeBankId(String userType) {
        Person person = new Person("John", "Smith", "213213");
        if(userType.equals("customer")){
            customer.setPerson(person);
            customer.setBankId(new BankId("fakeMasterKey"));
        } else if(userType.equals("merchant")){
            merchant.setPerson(person);
            merchant.setBankId(new BankId("fakeMasterKey"));
        }
    }
}
