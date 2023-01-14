import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankServiceService;
import dtu.ws.fastmoney.User;
import io.cucumber.java.en.Given;
import io.cucumber.java.After;
import io.cucumber.java.Before;

import jakarta.ws.rs.core.Response;
import messaging.Event;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;
import org.jboss.resteasy.spi.NotImplementedYetException;

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
    private DTUPayUser dtuPayUser;
    private CustomerAPI customerAPI = new CustomerAPI();


    @Before
    public void init() throws BankServiceException_Exception {
        customer.setFirstName("John0");
        customer.setLastName("Doe0");
        customer.setCprNumber("123456-8900");

        merchant.setFirstName("Jane0");
        merchant.setLastName("Doe0");
        merchant.setCprNumber("123456-8910");
        try {
            customerBankId = bankService.createAccountWithBalance(customer, BigDecimal.valueOf(1000));
            merchantBankId = bankService.createAccountWithBalance(merchant, BigDecimal.valueOf(2000));

        } catch (BankServiceException_Exception e) {
            throw new RuntimeException(e);
        }
        dtuPayCustomer.setBankId(new BankId(customerBankId));
        dtuPayCustomer.setPerson(new Person(customer.getFirstName(),customer.getLastName(),customer.getCprNumber()));
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
        Response response = customerAPI.postCustomer(dtuPayCustomer);

        var responseCode = response;
        assertTrue(201 == response.getStatus());
        // TODO: Clean up the test accounts
//        Event event = new Event("CustomerAccountCreated", new Object[] {  });
//        var response = publishedEvent.join();
       // var customer =  customerService.getCustomer(dtuPayCustomer.getBankId());
        //assertEquals(dtuPayCustomer, customer);

//        assertEquals(response.getType() ,"CustomerAccountCreated");

        //assertEquals(event,publishedEvent.join());
    }
}