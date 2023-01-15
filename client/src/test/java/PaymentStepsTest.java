import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankServiceService;
import dtu.ws.fastmoney.User;
import io.cucumber.java.en.Given;
import io.cucumber.java.After;
import io.cucumber.java.Before;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.ws.rs.core.Response;
import messaging.Event;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertTrue;

public class PaymentStepsTest {
    BankService bankService = new BankServiceService().getBankServicePort();
    User customer = new User();
    DTUPayUser dtuPayCustomer = new DTUPayUser();
    DTUPayUser dtuPayMerchant = new DTUPayUser();
    User merchant = new User();
    String customerBankId;
    String merchantBankId;

    private CompletableFuture<Event> publishedEvent = new CompletableFuture<>();

    private MessageQueue q =  new RabbitMqQueue("localhost");
    private CustomerService customerService = new CustomerService(q);
    private MerchantService merchantService = new MerchantService(q);
    private PaymentService paymentService = new PaymentService(q);

    private DTUPayUser registeredCustomer;
    private DTUPayUser registeredMerchant;

    private DTUPayUser dtuPayUser;
    private CustomerAPI customerAPI = new CustomerAPI();
    private MerchantAPI merchantAPI = new MerchantAPI();
    Token token;
    boolean success;


    @Before
    public void init() throws BankServiceException_Exception {
        customer.setFirstName("John0231111113112111111");
        customer.setLastName("Doe0231111111112311111");
        customer.setCprNumber("123456-390021111312111111111");

        merchant.setFirstName("Jane0231111111211113111");
        merchant.setLastName("Doe023111111131121111");
        merchant.setCprNumber("123456-391021111111231111111");
        try {
            customerBankId = bankService.createAccountWithBalance(customer, BigDecimal.valueOf(1000));
            merchantBankId = bankService.createAccountWithBalance(merchant, BigDecimal.valueOf(2000));

        } catch (BankServiceException_Exception e) {
            throw new RuntimeException(e);
        }
        dtuPayCustomer.setBankId(new BankId(customerBankId));
        dtuPayCustomer.setPerson(new Person(customer.getFirstName(),customer.getLastName(),customer.getCprNumber()));

        dtuPayMerchant.setBankId(new BankId(merchantBankId));
        dtuPayMerchant.setPerson(new Person(merchant.getFirstName(),merchant.getLastName(),merchant.getCprNumber()));

//        registeredCustomer = customerService.registerCustomer(dtuPayCustomer);
        //registeredMerchant = merchantService.registerMerchant(dtuPayMerchant);


//        new Thread(() -> {
//            registeredMerchant = customerService.registerMerchant(dtuPayMerchant);
//        }).start();
    }

    @After
    public void tearDown() {
        try {
            bankService.retireAccount(customerBankId);
            bankService.retireAccount(merchantBankId);
        } catch (BankServiceException_Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Given("^a customer registered with DTU Pay$")
    public void aCustomerRegisteredWithDTUPay() {
        dtuPayCustomer.setBankId(new BankId(customerBankId));
        dtuPayCustomer.setPerson(new Person(customer.getFirstName(),customer.getLastName(),customer.getCprNumber()));
        registeredCustomer = customerAPI.postCustomer(dtuPayCustomer);
        assertNotNull(registeredCustomer.getUniqueId());
    }
    @Given("^a merchant registered with DTU Pay$")
    public void aMerchantRegisteredWithDTUPay() {
        dtuPayMerchant.setBankId(new BankId(merchantBankId));
        dtuPayMerchant.setPerson(new Person(merchant.getFirstName(),merchant.getLastName(),merchant.getCprNumber()));
        registeredMerchant = merchantAPI.postMerchant(dtuPayMerchant);
        assertNotNull(registeredMerchant.getUniqueId());
    }

    @Given("a token associated with the customer")
    public void a_token_associated_with_the_customer() {
        token = customerAPI.requestToken(registeredCustomer.getUniqueId(),1);
    }
    @When("the merchant requests a transaction with the customer token")
    public void the_merchant_requests_a_transaction_with_the_customer_token() {
        // Write code here that turns the phrase above into concrete actions
        Transaction transaction = new Transaction(token,registeredMerchant.getUniqueId(), 100, "test");
        success = merchantAPI.postTransaction(transaction);
    }

    @Then("the transaction is successful")
    public void theTransactionIsSuccessful() {
        assertTrue(success);
    }
}