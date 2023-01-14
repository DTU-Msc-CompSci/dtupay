import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankServiceService;
import dtu.ws.fastmoney.User;
import io.cucumber.java.en.Given;
import io.cucumber.java.After;
import io.cucumber.java.Before;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;
import org.jboss.resteasy.spi.NotImplementedYetException;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

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


    @Before
    public void init() throws BankServiceException_Exception {
        customer.setFirstName("John34543");
        customer.setLastName("Doe43454");
        customer.setCprNumber("12344535456-78902");

        merchant.setFirstName("Jane445332");
        merchant.setLastName("Doe245433");
        merchant.setCprNumber("12344544356-78912");
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

        registeredCustomer = customerService.registerCustomer(dtuPayCustomer);
        registeredMerchant = merchantService.registerMerchant(dtuPayMerchant);


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
        assertNotNull(registeredCustomer.getUniqueId());
    }
    @Given("^a merchant registered with DTU Pay$")
    public void aMerchantRegisteredWithDTUPay() {
        assertNotNull(registeredMerchant.getUniqueId());
    }

    @When("the merchant requests a transaction")
    public void the_merchant_requests_a_transaction() {
        // Write code here that turns the phrase above into concrete actions
        Transaction transaction = new Transaction();
        transaction.setMerchantId(registeredMerchant.getUniqueId());
        transaction.setCustomerId(registeredCustomer.getUniqueId());
        transaction.setAmount(100);
        assertEquals("completed", paymentService.transactionRequest(transaction));
    }

}