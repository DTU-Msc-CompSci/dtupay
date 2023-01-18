import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

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

import org.acme.*;
import java.util.*;

public class RegistrationSteps {
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
        cost.setFirstName("Jrfedrfvvohn");
        cost.setLastName("Ramddfvfvbo");
        cost.setCprNumber("12ddfvf3123");

        User mer = new User();
        mer.setFirstName("Joddfvfvhn");
        mer.setLastName("Widfdfvvck");
        mer.setCprNumber("321dfv3dfv21");
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
            event = new Event("CustomerAccountCreated", new Object[]{ new AccountResponse(customer, "Success")});
        } else if(userType.equals("Merchant")) {
            event = new Event("MerchantAccountCreated", new Object[]{ new AccountResponse(merchant, "Success")});
        }
        verify(q).publish(event);
    }

    @When("the service receives a {word}AccountDeRegistrationRequested event")
    public void theServiceReceivesUserAccountDeRegistrationRequested(String userType) {
        if (userType.equals("Customer")) {
            Event event = new Event("CustomerAccountDeRegistrationRequested", new Object[]{customer.getUniqueId()});
            service.handleCustomerAccountDeRegistrationRequested(event);
        } else if(userType.equals("Merchant")) {
            Event event = new Event("MerchantAccountDeRegistrationRequested", new Object[]{merchant.getUniqueId()});
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

    @Then("a {word}AccountCreationFailed event is published because of {string}")
    public void aUserAccountCreationFailedEventPublished(String userType, String errorMsg) {
        Event event = null;
        if (userType.equals("Customer")) {
            event = new Event("CustomerAccountCreationFailed", new Object[]{ new AccountResponse(customer, errorMsg) });
        } else if(userType.equals("Merchant")) {
            event = new Event("MerchantAccountCreationFailed", new Object[]{ new AccountResponse(merchant, errorMsg) });
        }
        verify(q).publish(event);
    }

    @And("the {string} should exist in the database")
    public void userExists(String userType) {
        boolean exist = false;
        if(userType.equals("customer")) {
            exist = service.doesCustomerExist(customer.getBankId().getBankAccountId());
        } else if(userType.equals("merchant")) {
            exist = service.doesMerchantExist(merchant.getBankId().getBankAccountId());
        }
        assertTrue(exist);
    }

    @And("the {string} should not exist in the database")
    public void userDoesNotExist(String userType) {
        boolean exist = true;
        if(userType.equals("customer")) {
            exist = service.doesCustomerExist(customer.getBankId().getBankAccountId());
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

    @When("the service receives a TransactionRequested event")
    public void theServiceReceivesATransactionRequestedEvent() {
        Transaction transaction = new Transaction(new Token("fakeToken"), merchant.getUniqueId(), 100, "fakeTransactionID");
        Event event = new Event("TransactionRequested", new Object[]{ "random id", transaction });
        service.handleTransactionRequested(event);
    }

    @Then("a {word}InfoProvided event is published")
    public void aMerchantInfoProvidedEventIsPublished(String userType) {
        Event event = null;
        if(userType.equals("Customer")) {
            event = new Event("CustomerInfoProvided", new Object[]{ "random id", customer.getBankId().getBankAccountId()});
        }
        else if(userType.equals("Merchant")) {
            event = new Event("MerchantInfoProvided", new Object[]{ "random id", merchant.getBankId().getBankAccountId()});
        }
        verify(q).publish(event);
    }

    @When("the service receives a TokenValidated event")
    public void theServiceReceivesATokenValidatedEvent() {
        Event event = new Event("TokenValidated", new Object[]{  "random id", customer.getUniqueId() });
        service.handleTokenValidated(event);
    }
}
