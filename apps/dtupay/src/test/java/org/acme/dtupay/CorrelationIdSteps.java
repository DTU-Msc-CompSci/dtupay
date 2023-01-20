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
    MessageQueue mockQueue = mock(RabbitMqQueue.class);
    private final DTUPayService coreServiceMockRabbit = new DTUPayService(mockQueue);
    private DTUPayUser customer;

    @Given("existing customer with bank ID {string}")
    public void existingCustomerWithBankID(String bankId) {
        customer = new DTUPayUser();
        customer.setBankId(new BankId(bankId));
        customer.setPerson(new Person("Emerita", "Martinez", "110998-3000"));
    }

    @When("the customer registers for DTU Pay")
    public void theCustomerRegistersForDTUPay() {
        new Thread(() -> {
            coreServiceMockRabbit.registerCustomer(customer);
        }).start();
    }

    @Then("the event is sent to be processed with a correlation ID")
    public void theEventIsSentToBeProcessedWithACorrelationID() {
        mockQueue.addHandler("CustomerAccountCreationRequested", e -> {
            var correlationId = e.getArgument(0, String.class);
            var s = e.getArgument(1, DTUPayUser.class);
            var eventType = e.getType();
            assertEquals(customer, s);
            assertEquals("CustomerAccountCreationRequested", eventType);
            assertNotNull(correlationId);
        });
    }

    @Then("the CustomerAccountCreated event has a correlation ID")
    public void theCustomerAccountCreatedEventHasACorrelationID() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        customer.setUniqueId(UUID.randomUUID().toString());
        var correlationId = coreServiceMockRabbit.getPendingCustomers().entrySet().iterator().next().getKey();
        coreServiceMockRabbit.handleCustomerRegistered(new Event("CustomerAccountCreated", new Object[]{correlationId, customer}));
        assertTrue(coreServiceMockRabbit.getPendingCustomers().isEmpty());
    }
}
