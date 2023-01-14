import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankServiceService;
import dtu.ws.fastmoney.User;
import io.cucumber.java.en.Given;
import io.cucumber.java.After;
import io.cucumber.java.Before;

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
    private CompletableFuture<DTUPayUser> registeredCustomer = new CompletableFuture<>();
    private DTUPayUser dtuPayUser;


    @Before
    public void init() throws BankServiceException_Exception {
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setCprNumber("123456-7890");

        merchant.setFirstName("Jane");
        merchant.setLastName("Doe");
        merchant.setCprNumber("123456-7891");
        try {
            customerBankId = bankService.createAccountWithBalance(customer, BigDecimal.valueOf(1000));
            merchantBankId = bankService.createAccountWithBalance(merchant, BigDecimal.valueOf(2000));

        } catch (BankServiceException_Exception e) {
            throw new RuntimeException(e);
        }
        dtuPayCustomer.setBankId(new BankId(customerBankId));
        dtuPayCustomer.setPerson(new Person(customer.getFirstName(),customer.getLastName(),customer.getCprNumber()));

        new Thread(() -> {
            var result = customerService.register(dtuPayCustomer);
            registeredCustomer.complete(result);
        }).start();
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
        // TODO: Clean up the test accounts
//        Event event = new Event("CustomerAccountCreated", new Object[] {  });
//        var response = publishedEvent.join();
       // var customer =  customerService.getCustomer(dtuPayCustomer.getBankId());
        //assertEquals(dtuPayCustomer, customer);

//        assertEquals(response.getType() ,"CustomerAccountCreated");

        //assertEquals(event,publishedEvent.join());
    }
}