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
    //private DTUPayUser merchant = new DTUPayUser();

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
    public void aCustomerAccountCreationRequestedPublished() {
        Event event = new Event("CustomerAccountCreated", new Object[] {customer});
        verify(q).publish(event);
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
