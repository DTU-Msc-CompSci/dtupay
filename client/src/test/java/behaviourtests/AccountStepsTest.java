package behaviourtests;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankServiceService;
import dtu.ws.fastmoney.User;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class AccountStepsTest {

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
    ErrorMessageHolder errorMessageHolder = new ErrorMessageHolder();
    private Response response;



    @Before
    public void init() throws BankServiceException_Exception {
        customer.setFirstName("Aleeerfrsds");
        customer.setLastName("tseftgeees");
        customer.setCprNumber("1vffres");



        merchant.setFirstName("Som3rertgtsv");
        merchant.setLastName("rwssddtssvererna3smes23");
        merchant.setCprNumber("wdwdsqqwdqd");

        merchantBankId = bankService.createAccountWithBalance(merchant, BigDecimal.valueOf(1000));
        customerBankId = bankService.createAccountWithBalance(customer, BigDecimal.valueOf(1000));

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
    public void theCustomerDeRegisters() throws Exception {
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
        System.out.println(registeredCustomer.getUniqueId());
        assertNull(registeredCustomer.getUniqueId());
    }

    @Then("the customer gets an error message")
    public void theCustomerGetsAnErrorMessage() {
        assertEquals("Account does not exist in DTUPay", errorMessageHolder.getErrorMessage());
    }

    @Given("a customer with the first name {string}, last name {string}, and CPR number {string}")
    public void aCustomerWithTheFirstNameLastNameAndCPRNumber(String first, String last, String cpr) {
        dtuPayCustomer.setPerson(new Person(first,last,cpr));

    }

    @And("a bank account for the customer")
    public void aBankAccountWithABalanceOf() {
        dtuPayCustomer.setBankId(new BankId(customerBankId));
    }

    @When("the customer tries to register")
    public void theCustomerTriesToRegister() throws Exception {
        registeredCustomer = customerAPI.postCustomer(dtuPayCustomer);
    }

    @Then("registration is successful for the customer")
    public void registrationIsSuccessful() {
        assertNotNull(registeredCustomer.getUniqueId());
    }

    @Given("a merchant with the first name {string}, last name {string}, and CPR number {string}")
    public void aMerchantWithTheFirstNameLastNameAndCPRNumber(String first, String last, String cpr) {
        dtuPayMerchant.setPerson(new Person(first,last,cpr));
    }

    @And("a bank account for the merchant")
    public void aBankAccountForTheMerchant() {
        dtuPayMerchant.setBankId(new BankId(merchantBankId));
    }

    @When("the merchant tries to register")
    public void theMerchantTriesToRegister() throws Exception {
        registeredMerchant = merchantAPI.postMerchant(dtuPayMerchant);
    }

    @Then("registration is successful for the merchant")
    public void registrationIsSuccessfulForTheMerchant() {
        assertNotNull(registeredMerchant.getUniqueId());
    }
}
