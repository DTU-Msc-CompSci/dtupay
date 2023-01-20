import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import org.acme.Token;
import org.acme.TokenRequest;
import org.acme.TokenResponse;
import org.acme.TokenService;
import org.acme.Transaction;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import messaging.MessageQueue;

import java.util.UUID;

public class TokenSteps {
    private MessageQueue q = mock(MessageQueue.class);
    private TokenService service = new TokenService(q);
    String customerID;
    int amount;
    String correlationID = UUID.randomUUID().toString();

    String tokenID;
    @Given("a customer id {string}")
    public void aCustomerId(String customerID) {
        this.customerID = customerID;
    }

    @And("the id is in the token map")
    public void theIdIsInTheTokenMap() {
        Event event = new Event("TokenUserRequested", new Object[]{ customerID });
        service.handleTokenUserAdd(event);
        assertTrue(service.getAssignedTokens().containsKey(customerID));
    }

    @And("a token request of {int}")
    public void aTokenRequestOf(int amount) {
        this.amount = amount;
    }

    @When("the service receives a TokenRequested event")
    public void theServiceReceivesATokenRequestedEvent() {
        Event event = new Event("TokenRequested",new Object[]{ correlationID, new TokenRequest(customerID,amount)});
        assertNotNull(event);
        service.handleTokenRequested(event);
    }

    @Then("a TokenRequestFulfilled event is published")
    public void aTokenRequestFulfilledEventIsPublished() {
        //Event event = new Event("TokenRequestFulfilled", new Object[] { new TokenResponse(service.getAssignedTokens().get(customerID),"success")});
        TokenResponse response = new TokenResponse(service.getAssignedTokens().get(customerID),"success");
        Event event = new Event("TokenRequestFulfilled", new Object[] { correlationID, response });
        //q.publish(event);
        verify(q).publish(event);
    }

    @And("{int} tokens should exist for the user")
    public void tokensShouldExistForTheUser(int amount) {
        assertTrue((service.getAssignedTokens().get(customerID) == null && amount == 0) || service.getAssignedTokens().get(customerID).size() == amount);
        if (amount != 0) {
            for (Token t : service.getAssignedTokens().get(customerID)) {
                assertNotNull(t.getToken());
            }
        }
    }

    @Then("a TokenRequestFailed event is published saying {string}")
    public void aTokenRequestFailedEventIsPublishedSaying(String message) {
        TokenResponse response = new TokenResponse();
        response.setMessage(message);
        Event event = new Event("TokenRequestFailed",new Object[] { correlationID, response });
        verify(q).publish(event);
    }

    @And("the customer has {int} tokens")
    public void theCustomerHasTokens(int amount) {
        service.generateTokens(new TokenRequest(customerID,amount));
    }

    @When("the service receives a TokenUserRequested event")
    public void theServiceReceivesATokenUserRequestedEvent() {
        Event event = new Event("TokenUserRequested",new Object[]{ customerID });
        assertNotNull(event);
        service.handleTokenUserAdd(event);
    }

    @Then("assignedTokens contains the customer id")
    public void assignedtokensContainsTheCustomerId() {
        assertTrue(service.getAssignedTokens().containsKey(customerID));
    }

    @When("the service receives a CustomerAccountDeRegistrationRequested event")
    public void theServiceReceivesACustomerAccountDeRegistrationRequestedEvent() {
        Event event = new Event("CustomerAccountDeRegistrationRequested",new Object[]{ correlationID, customerID });
        assertNotNull(event);
        service.handleRemoveAllTokenFromDeRegisteredCustomer(event);
    }

    @Then("a AllTokenRemovedFromDeRegisteredCustomer event is published")
    public void aAllTokenRemovedFromDeRegisteredCustomerEventIsPublished() {
        Event event = new Event("AllTokenRemovedFromDeRegisteredCustomer",new Object[] { correlationID, true });
        verify(q).publish(event);
    }

    @And("assignedTokens does not contain the customer id")
    public void assignedtokensDoesNotContainTheCustomerId() {
        assertFalse(service.getAssignedTokens().containsKey(customerID));
    }

    @And("tokenToId does not contain the customer id as a value")
    public void tokentoidDoesNotContainTheCustomerIdAsAValue() {
        assertFalse(service.getTokenToId().containsValue(customerID));
    }

    @And("the customer has {int} token with the id {string}")
    public void theCustomerHasTokenWithTheId(int amount, String tokenID) {
        this.amount = amount;
        this.tokenID = tokenID;

        service.addToken(customerID,tokenID);
    }


   @When("the service receives a TransactionRequested event")
   public void theServiceReceivesATransactionRequestedEvent() {
        Transaction transaction = new Transaction(new Token(this.tokenID), "test", this.amount, correlationID);
        System.out.println(transaction);
        Event event = new Event("TransactionRequested", new Object[] { correlationID, transaction });
        assertNotNull(event);
        service.handleTransactionRequested(event);

   }

   @Then("a TokenValidated event is published containing the customer id")
   public void aTokenValidatedEventIsPublished() {
        Event event = new Event("TokenValidated", new Object[] { correlationID, customerID });
        verify(q).publish(event);
   }
}
