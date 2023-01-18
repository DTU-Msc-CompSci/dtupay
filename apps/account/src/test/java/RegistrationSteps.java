import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

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

    @Given("There is a CustomerAccountCreationRequested event in the queue")
    public void thereIsACustomerAccountCreationRequestedEventInTheQueue() {
//        realQueue.publish(new Event("CustomerAccountCreationRequested", new Object[] { UUID.randomUUID().toString(), customer}));
        // synthetic sleep to allow the event to be processed
        var corId = UUID.randomUUID().toString();
        mockQueue.publish(new Event("CustomerAccountCreationRequested", new Object[] { corId, customer}));
        verify(mockQueue).publish(new Event("CustomerAccountCreationRequested", new Object[] { corId, customer}));
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
    }
}
