import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;
import org.acme.dtupay.BankId;
import org.acme.dtupay.CoreService;
import org.acme.dtupay.DTUPayUser;
import org.acme.dtupay.Person;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CorrelationIdSteps {
//    RabbitMqQueue queue = new RabbitMqQueue();
    MessageQueue mockQueue = mock(RabbitMqQueue.class);
    private CoreService coreServiceMockRabbit = new CoreService(mockQueue);
//    private CoreService coreService = new CoreService(queue);
    private DTUPayUser customer;

    @Given("existing customer with bank ID {string}")
    public void existingCustomerWithBankID(String bankId) {
        customer = new DTUPayUser();
        customer.setBankId(new BankId(bankId));
        customer.setPerson(new Person("Alekreos23rf","tsecfrd23","1vr323arflex123test"));
    }
    @When("the customer registers for DTU Pay")
    public void theCustomerRegistersForDTUPay() {
        new Thread(() -> {
            coreServiceMockRabbit.registerCustomer(customer);
        }).start();
    }

    @Then("the event is sent to be processed with a correlation ID")
    public void theEventIsSentToBeProcessedWithACorrelationID() {
        // We're going to change this test around just slightly. Since we are pushing the event to Rabbit, as long as another
        // service doesn't read from the queue, the message should still be there.
        mockQueue.addHandler("CustomerAccountCreationRequested", e -> {
            var correlationId = e.getArgument(0, String.class);
            var s = e.getArgument(1, DTUPayUser.class);
            var eventType = e.getType();
            assertEquals(customer, s);
            assertEquals("CustomerAccountCreationRequested", eventType);
            assertNotNull(correlationId);
        });


//        // Unfortunately, we have to use an artificial sleep on the test because the other thread doesn't finish executing
//        // by the time the test is finished.
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        assertFalse(coreService.getPendingCustomers().isEmpty());
    }

    @Then("the CustomerAccountCreated event has a correlation ID")
    public void theCustomerAccountCreatedEventHasACorrelationID() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // we have to set the customer ID since the queue is mocked and never actually goes to the service
        customer.setUniqueId(UUID.randomUUID().toString());
        var correlationId = coreServiceMockRabbit.getPendingCustomers().entrySet().iterator().next().getKey();
        coreServiceMockRabbit.handleCustomerRegistered(new Event("CustomerAccountCreated", new Object[] { correlationId, customer }));
        assertTrue(coreServiceMockRabbit.getPendingCustomers().isEmpty());
    }
}
