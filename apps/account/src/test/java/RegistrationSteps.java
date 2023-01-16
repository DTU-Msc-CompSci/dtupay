import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    private AccountService service = new AccountService(q);
    private CompletableFuture<DTUPayUser> registeredUser = new CompletableFuture<>();
    private Person person;

    private DTUPayUser customer = new DTUPayUser();

    String result;

    public RegistrationSteps() {
    }

    @Given("There is a costumer with empty id")
    public void thereIsAUserWithEmptyId() {
        person = new Person("John", "Magkas", "420666");
        customer.setPerson(person);
        customer.setBankId(new BankId("customerBankId"));
        assertNull(customer.getUniqueId());
    }

    @When("the customer is being registered")
    public void theCustomerIsBeingRegistered() {
        result = service.addCustomer(customer);
    }

    @Then("The customer added correctly")
    public void theEventIsSent() {
        customer.setUniqueId(result);
        String reg_user = service.getCustomer(result);
        String opt = customer.getBankId().getBankAccountId();
        assertEquals(reg_user, opt);


    }
}
