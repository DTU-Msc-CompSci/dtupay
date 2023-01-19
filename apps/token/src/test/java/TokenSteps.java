import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import org.acme.Token;
import org.acme.TokenRequest;
import org.acme.TokenResponse;
import org.acme.TokenService;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import messaging.MessageQueue;

import java.util.UUID;

public class TokenSteps {
    private MessageQueue q = mock(MessageQueue.class);
    private TokenService service = new TokenService(q);
    String customerID;
    int amount;
    String correlationID = UUID.randomUUID().toString();

    @Given("a customer id {string}")
    public void aCustomerId(String customerID) {
        this.customerID = customerID;
    }

    @And("the id is in the token map")
    public void theIdIsInTheTokenMap() {
        Event event = new Event("TokenUserRequested", new Object[]{ customerID });
        service.handleTokenUserAdd(event);
        assertTrue(service.getAssignedTokens().keySet().contains(customerID));
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
        assertTrue(service.getAssignedTokens().get(customerID).size() == amount);
        for(Token t : service.getAssignedTokens().get(customerID)) {
            assertNotNull(t.getToken());
        }
    }
}
