package behaviourtests;

import dtu.ws.fastmoney.User;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.smallrye.common.constraint.Assert.assertNotNull;
import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TokenStepsTest {

    CustomerAPI customerAPI = new CustomerAPI();
    DTUPayUser customer = new DTUPayUser();
    Set<Token> tokens = new HashSet<Token>();
    String error;

    @Given("a customer is registered with DTU Pay")
    public void a_customer_is_registered_with_DTU_Pay() {
        customer.setBankId(new BankId("test"));
        customer.setPerson(new Person("test","test","test"));
        customer = customerAPI.postCustomer(customer);
        assertNotNull(customer.getUniqueId());
    }

    @When("the customer requests {int} tokens")
    public void a_customer_requests_tokens(int amount) throws Exception {
        try {
            tokens = customerAPI.requestToken(customer.getUniqueId(),amount);
        } catch (Exception e){
            error = e.getMessage();
        }
    }

    @Then("the customer receives {int} tokens")
    public void theCustomerReceivesTokens(int amount) {
        assertTrue(tokens.size() == amount);
        for(Token t : tokens) {
            assertNotNull(t.getToken());
        }
    }

    @And("the customer has already requested {int} unused tokens")
    public void theCustomerHasAlreadyRequestedUnusedTokens(int amount) throws Exception {
        customerAPI.requestToken(customer.getUniqueId(),amount);
    }

    @Then("the token request fails and throws an exception {string}")
    public void theTokenRequestFailsAndThrowsAnException(String message) {
        assertEquals(message,error);
    }

    @Given("a customer that is not registered with DTU Pay")
    public void aCustomerThatIsNotRegisteredWithDTUPay() {
        customer.setBankId(new BankId("test"));
        customer.setPerson(new Person("test","test","test"));
        assertNull(customer.getUniqueId());
    }
}
