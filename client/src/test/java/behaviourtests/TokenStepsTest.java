package behaviourtests;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceService;
import dtu.ws.fastmoney.User;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;
import java.util.Set;

import static io.smallrye.common.constraint.Assert.assertNotNull;
import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TokenStepsTest {

    BankService bankService = new BankServiceService().getBankServicePort();
    CustomerAPI customerAPI = new CustomerAPI();
    DTUPayUser customer = new DTUPayUser();
    Set<Token> tokens;
    String error;
    String bankID;

    @Before
    public void before() {
        User c = new User();
        c.setFirstName("Alesafffsf3fd2ffdsfdsddxst");
        c.setLastName("testfafaff3afd2dfsfsdfssdsdAlex");
        c.setCprNumber("somfe3Rfaffsd2dffasdfdasdndomStuff");
        try {
            bankID = bankService.createAccountWithBalance(c, BigDecimal.valueOf(1000));
        } catch (Exception e) {
            error = e.getMessage();
            System.out.println(error);
        }
    }

    @After
    public void after() {
        try {
            bankService.retireAccount(bankID);
        } catch (Exception e) {
            error = e.getMessage();
            System.out.println(error);
        }

    }

    @Given("a customer is registered with DTU Pay")
    public void a_customer_is_registered_with_DTU_Pay() throws Exception {
        customer.setBankId(new BankId(bankID));
        customer.setPerson(new Person("Alextest", "testAlex", "someRandomStuff"));
        customer = customerAPI.postCustomer(customer);
    }

    @When("the customer requests {int} tokens")
    public void a_customer_requests_tokens(int amount) {
        try {
            tokens = customerAPI.requestToken(customer.getUniqueId(), amount);
        } catch (Exception e) {
            error = e.getMessage();
        }
    }

    @Then("the customer receives {int} tokens")
    public void theCustomerReceivesTokens(int amount) {
        assertTrue(tokens.size() == amount);
        for (Token t : tokens) {
            assertNotNull(t.getToken());
        }
    }

    @And("the customer has already requested {int} unused tokens")
    public void theCustomerHasAlreadyRequestedUnusedTokens(int amount) throws Exception {
        customerAPI.requestToken(customer.getUniqueId(), amount);
    }

    @Then("the token request fails and throws an exception {string}")
    public void theTokenRequestFailsAndThrowsAnException(String message) {
        assertEquals(message, error);
    }

    @Given("a customer that is not registered with DTU Pay")
    public void aCustomerThatIsNotRegisteredWithDTUPay() {
        customer.setBankId(new BankId(bankID));
        customer.setPerson(new Person("test", "test", "test"));
        assertNull(customer.getUniqueId());
    }
}
