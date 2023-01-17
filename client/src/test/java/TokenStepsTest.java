import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;

import static org.junit.Assert.*;

public class TokenStepsTest {

    CustomerAPI customerAPI = new CustomerAPI();
    DTUPayUser customer;
    List<Token> tokens;
    String error;

    @Given("a customer registered with DTU Pay")
    public void a_customer_registered_with_dtu_pay() {
        customer = customerAPI.postCustomer(new DTUPayUser());
        assertNotNull(customer.getUniqueId());
    }

    @When("the customer requests {int} tokens")
    public void the_customer_requests_tokens(int amount) {
        try {
            tokens = customerAPI.requestToken(customer.getUniqueId(),amount);
        } catch (Exception e) {
            error = e.getMessage();
        }

    }

    @Then("the customer receives {int} tokens")
    public void the_customer_receives_tokens(int amount) {
        assertTrue(tokens.size() == amount);
        for(Token t : tokens) {
            assertNotNull(t.getToken());
        }
    }

    @Given("the customer has already requested {int} unused tokens")
    public void the_customer_has_already_requested_unused_tokens(int amount) {
        customerAPI.requestToken(customer.getUniqueId(),amount);
    }

    @Then("the token request fails and throws an exception {string}")
    public void the_token_request_fails_and_throws_an_exception(String message) {
        assertEquals(error,message);
    }

}
