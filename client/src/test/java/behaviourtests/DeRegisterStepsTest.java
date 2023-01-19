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
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

public class DeRegisterStepsTest {

    BankService bankService = new BankServiceService().getBankServicePort();
    private CustomerAPI customerAPI = new CustomerAPI();
    private MerchantAPI merchantAPI = new MerchantAPI();

    User customer = new User();
    User merchant = new User();

    DTUPayUser dtuPayCustomer = new DTUPayUser();
    DTUPayUser dtuPayMerchant = new DTUPayUser();

    String customerBankId;
    String merchantBankId;

    private DTUPayUser registeredCustomer;
    private DTUPayUser registeredMerchant;
    private DTUPayUser deRegisteredCustomer;
    private Response response;



    @Before
    public void init() throws BankServiceException_Exception {
        customer.setFirstName("Aleeerefrsdfsdvtsstgssbrtvrsasdfdfvsssdedeesfdddddd");
        customer.setLastName("tseftgeersertrvssssstvwsdsdwdfbcfresssed2s3dddddddd");
        customer.setCprNumber("1vffrtgeberrtwdsssssdwdvt323arflex123ssstees33stdddddddddd");

        customerBankId = bankService.createAccountWithBalance(customer, BigDecimal.valueOf(1000));

        merchant.setFirstName("Som3rertgrrtvtssvfrwdwdfererfveO23thersNam3es");
        merchant.setLastName("ncvrrftgeeftrvrwssddtvedererna3sme23");
        merchant.setCprNumber("321altgrefrtvrtwssdwedcsdvdfbbrfffereex23s3321test");

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
        response = customerAPI.deregisterCustomer(registeredCustomer);
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
}
