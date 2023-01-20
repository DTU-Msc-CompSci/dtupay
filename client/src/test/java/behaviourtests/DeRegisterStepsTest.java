package behaviourtests;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankServiceService;
import dtu.ws.fastmoney.User;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class DeRegisterStepsTest {

    BankService bankService = new BankServiceService().getBankServicePort();
    private final CustomerAPI customerAPI = new CustomerAPI();
    private final MerchantAPI merchantAPI = new MerchantAPI();

    User customer = new User();
    User merchant = new User();

    DTUPayUser dtuPayCustomer = new DTUPayUser();
    DTUPayUser dtuPayMerchant = new DTUPayUser();

    String customerBankId;
    String merchantBankId;

    private DTUPayUser registeredCustomer;
    private DTUPayUser registeredMerchant;
    private DTUPayUser deRegisteredCustomer;
    ErrorMessageHolder errorMessageHolder = new ErrorMessageHolder();
    private Response response;



   @Before
   public void init() throws BankServiceException_Exception {
       customer.setFirstName("alex");
       customer.setLastName("Travolta");
       customer.setCprNumber("masteCPRvalue");

       customerBankId = bankService.createAccountWithBalance(customer, BigDecimal.valueOf(1000));

       merchant.setFirstName("Voldemor");
       merchant.setLastName("_");
       merchant.setCprNumber("_");

       merchantBankId = bankService.createAccountWithBalance(merchant, BigDecimal.valueOf(1000));


   }

   @After
   public void tearDown() {
       try {
           bankService.retireAccount(customerBankId);
           bankService.retireAccount(merchantBankId);
       } catch (BankServiceException_Exception e) {
           //throw new RuntimeException(e);
       }
   }

    @Given("a customer exists in DTUPay")
    public void aCustomerExistsInDTUPay() {
        dtuPayCustomer.setPerson(new Person(customer.getFirstName(), customer.getLastName(), customer.getCprNumber()));
        dtuPayCustomer.setBankId(new BankId(customerBankId));

        try {
            registeredCustomer = customerAPI.postCustomer(dtuPayCustomer);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        assertNotNull(registeredCustomer.getUniqueId());
    }

    @When("the customer de-registers")
    public void theCustomerDeRegisters() {
        try {
            response = customerAPI.deregisterCustomer(registeredCustomer);
            System.out.println(response.getStatus());
        } catch (Exception e) {
            errorMessageHolder.setErrorMessage(e.getMessage());
        }
    }

    @Then("the customer is removed from DTUPay")
    public void theCustomerIsRemovedFromDTUPay() {
        assertEquals(200,response.getStatus());
    }

    @Given("a merchant exists in DTUPay")
    public void aMerchantExistsInDTUPay() {
        dtuPayMerchant.setPerson(new Person(merchant.getFirstName(), merchant.getLastName(), merchant.getCprNumber()));
        dtuPayMerchant.setBankId(new BankId(merchantBankId));

        try {
            registeredMerchant = merchantAPI.postMerchant(dtuPayMerchant);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        assertNotNull(registeredMerchant.getUniqueId());
    }

    @When("the merchant de-registers")
    public void theMerchantDeRegisters() {
        response = merchantAPI.deregisterMerchant(registeredMerchant);
    }

    @Then("the merchant is removed from DTUPay")
    public void theMerchantIsRemovedFromDTUPay() {
        assertEquals(200,response.getStatus());
    }

    @Given("a customer does not exist in DTUPay")
    public void aCustomerDoesNotExistInDTUPay() {
        dtuPayCustomer.setPerson(new Person(customer.getFirstName(), customer.getLastName(), customer.getCprNumber()));
        dtuPayCustomer.setBankId(new BankId(customerBankId));
        registeredCustomer = dtuPayCustomer;
        assertNull(registeredCustomer.getUniqueId());
    }

    @Then("the customer gets an error message")
    public void theCustomerGetsAnErrorMessage() {
        System.out.println(errorMessageHolder.getErrorMessage());
        assertEquals("Account does not exist in DTUPay", errorMessageHolder.getErrorMessage());
    }
}
