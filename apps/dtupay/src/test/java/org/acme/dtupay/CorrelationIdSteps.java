package org.acme.dtupay;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class CorrelationIdSteps {
//    RabbitMqQueue queue = new RabbitMqQueue();
    MessageQueue mockQueue = mock(RabbitMqQueue.class);
    private CoreService coreService = new CoreService(mockQueue);
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
            coreService.registerCustomer(customer);
        }).start();
    }

    @Then("the event is sent to be processed with a correlation ID")
    public void theEventIsSentToBeProcessedWithACorrelationID() {
        // Unfortunately, we have to use an artificial sleep on the test because the other thread doesn't finish executing
        // by the time the test is finished.
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertFalse(coreService.getPendingCustomers().isEmpty());
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
        var correlationId = coreService.getPendingCustomers().entrySet().iterator().next().getKey();
        coreService.handleCustomerRegistered(new Event("CustomerAccountCreated", new Object[] { correlationId, customer }));
        assertTrue(coreService.getPendingCustomers().isEmpty());
    }
}
