import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

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
    private String costumerUniqueId = null;     //Store costumer's uniqueID in order to delete the costumer at the end
    private DTUPayUser merchant = new DTUPayUser();
    private String merchantUniqueId = null;     //Store merchant's uniqueID in order to delete the costumer at the end

    public RegistrationSteps() {
    }

    @Given("There is a costumer with empty id")
    public void thereIsACostumerWithEmptyId() {
        Person person = new Person("John", "Magkas", "123123");
        customer.setPerson(person);
        customer.setBankId(new BankId("customerBankId"));
        assertNull(customer.getUniqueId());
    }

    @When("the service receives a CustomerAccountCreationRequested event")
    public void theServiceReceivesCustomerAccountCreationRequestedEvent(){
        var event = new  Event("CustomerAccountCreationRequested", new Object[] {customer});
        customer.setUniqueId(service.handleCustomerAccountCreationRequested(event));
    }

    @Then("a CustomerAccountCreated event is published")
    public void aCustomerAccountCreatedPublished() {
        costumerUniqueId = customer.getUniqueId();
        Event event = new Event("CustomerAccountCreated", new Object[] {customer});
        verify(q).publish(event);
    }

    @When("the service receives a CustomerAccountDeRegistrationRequested event")
    public void theServiceReceivesCustomerAccountDeRegistrationRequested(){
        Event event = new Event("CustomerAccountDeRegistrationRequested", new Object[] {customer.getUniqueId()});
        service.handleCustomerAccountDeRegistrationRequested(event);
    }

    @Then("a CustomerAccountDeRegistrationCompleted event is published")
    public void aCustomerAccountDeRegistrationCompletedPublished() {
        Event event = new Event("CustomerAccountDeRegistrationCompleted", new Object[] {true});
        verify(q).publish(event);
    }

    @Then("a CustomerAccountFailed event is published")
    public void aCustomerAccountFailedPublished() {
        Event event = new Event("CostumerAccountCreationFailed", new Object[] {false});
        verify(q).publish(event);
    }

    @Given("There is a merchant with empty id")
    public void thereIsAMerchantWithEmptyId() {
        Person person = new Person("John", "Wick", "321321");
        merchant.setPerson(person);
        merchant.setBankId(new BankId("customerBankId"));
        assertNull(merchant.getUniqueId());
    }

    @When("the service receives a MerchantAccountCreationRequested event")
    public void theServiceReceivesMerchantAccountCreationRequestedEvent(){
        var event = new  Event("MerchantAccountCreationRequested", new Object[] {merchant});
        merchant.setUniqueId(service.handleMerchantAccountCreationRequested(event));
    }

    @Then("a MerchantAccountCreated event is published")
    public void aMerchantAccountCreationRequestedPublished() {
        merchantUniqueId = merchant.getUniqueId();
        Event event = new Event("MerchantAccountCreated", new Object[] {merchant});
        verify(q).publish(event);
    }

    @When("the service receives a MerchantAccountDeRegistrationRequested event")
    public void theServiceReceivesMerchantAccountDeRegistrationRequested(){
        Event event = new Event("MerchantAccountDeRegistrationRequested", new Object[] {merchant.getUniqueId()});
        service.handleMerchantAccountDeRegistrationRequested(event);
    }

    @Then("a MerchantAccountDeRegistrationCompleted event is published")
    public void aMerchantAccountDeRegistrationCompletedPublished() {
        Event event = new Event("MerchantAccountDeRegistrationCompleted", new Object[] {true});
        verify(q).publish(event);
    }

    @Then("a MerchantAccountCreationFailed event is published")
    public void aMerchantAccountFailedPublished() {
        Event event = new Event("MerchantAccountCreationFailed", new Object[] {false});
        verify(q).publish(event);
    }

    @Then("Does costumer exists")
    public void doesCostumerExists() {
        boolean result = service.doesCostumerExist(customer);
        assertTrue(result);
    }

//    @Given("There is a merchant with empty id")
//    public void thereIsAMerchantWithEmptyId() {
//        Person person = new Person("John", "Wick", "321321");
//        customer.setPerson(person);
//        customer.setBankId(new BankId("customerBankId"));
//        assertNull(customer.getUniqueId());
//    }
//    @When("the service receives a MerchantAccountCreationRequested event")
//
//    @When("the customer is being registered")
//    public void theCustomerIsBeingRegistered() {
//        result = service.addCustomer(customer);
//    }
//
//    @When("the merchant is being registered")
//    public void theMerchantIsBeingRegistered() {
//        result = service.addMerchant(merchant);
//    }
//
//    @Then("The merchant added correctly")
//    @Then("The customer added correctly")
//    public void theEventIsSent() {
//        String reg_user = service.getCustomer(result);
//        String opt = customer.getBankId().getBankAccountId();
//        assertEquals(reg_user, opt);
//    }
}
