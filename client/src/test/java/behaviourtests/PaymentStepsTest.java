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

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

public class PaymentStepsTest {
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

    Token token;

    boolean success;


    @Before
    public void init() throws BankServiceException_Exception {
        customer.setFirstName("Aleeerefrvttgbrtvrsasdfdfvdedf");
        customer.setLastName("tseftgeerertrvtvfbcfrd23");
        customer.setCprNumber("1vffrtgeberrtvt323arflex123test");

        merchant.setFirstName("Som3rertgrrtvtvfrfererfveO23therNames");
        merchant.setLastName("ncvrrftgeeftrvrtvederername23");
        merchant.setCprNumber("321altgrefrtvrtedcsdvdfbbrfffereex23321test");
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
    @Given("^a customer registered with DTU Pay$")
    public void aCustomerRegisteredWithDTUPay() {
        try {
            registeredCustomer = customerAPI.postCustomer(dtuPayCustomer);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            //assertFalse(true);
        }
        assertNotNull(registeredCustomer.getUniqueId());
    }
    @Given("^a merchant registered with DTU Pay$")
    public void aMerchantRegisteredWithDTUPay() {
        dtuPayMerchant.setBankId(new BankId(merchantBankId));
        dtuPayMerchant.setPerson(new Person(merchant.getFirstName(),merchant.getLastName(),merchant.getCprNumber()));
        try {
            registeredMerchant = merchantAPI.postMerchant(dtuPayMerchant);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        assertNotNull(registeredMerchant.getUniqueId());
    }

    @Given("a token associated with the customer")
    public void a_token_associated_with_the_customer() throws Exception {
        Set<Token> tokens = customerAPI.requestToken(registeredCustomer.getUniqueId(),1);
        token = tokens.iterator().next();
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

    @And("the balance of the customer at the bank is {int} kr")
    public void theBalanceOfTheCustomerAtTheBankIsKr(int arg0) {
        try {
            assertEquals(BigDecimal.valueOf(arg0), bankService.getAccount(dtuPayCustomer.getBankId().getBankAccountId()).getBalance());
        } catch (BankServiceException_Exception e) {
            assertTrue(false);
        }
    }

    @And("the balance of the merchant at the bank is {int} kr")
    public void theBalanceOfTheMerchantAtTheBankIsKr(int arg0) {
        try {
            assertEquals(BigDecimal.valueOf(arg0), bankService.getAccount(dtuPayMerchant.getBankId().getBankAccountId()).getBalance());
        } catch (BankServiceException_Exception e) {
            assertTrue(false);
        }
    }

    @Given("a customer with a bank account with balance {int}")
    public void aCustomerWithABankAccountWithBalance(int arg0) {
        try {
            customerBankId = bankService.createAccountWithBalance(customer, BigDecimal.valueOf(arg0));
            dtuPayCustomer.setBankId(new BankId(customerBankId));
            dtuPayCustomer.setPerson(new Person(customer.getFirstName(),customer.getLastName(),customer.getCprNumber()));
        } catch (BankServiceException_Exception e) {
            assertTrue(false);
        }
    }

    @Given("a merchant with a bank account with balance {int}")
    public void aMerchantWithABankAccountWithBalance(int amount) {
        try {
            merchantBankId = bankService.createAccountWithBalance(merchant, BigDecimal.valueOf(amount));
            dtuPayMerchant.setBankId(new BankId(merchantBankId));
            dtuPayMerchant.setPerson(new Person(merchant.getFirstName(),merchant.getLastName(),merchant.getCprNumber()));
        } catch (BankServiceException_Exception e) {
            assertTrue(false);
        }
    }

    @When("the merchant initiates a payment for {int} kr with the customer token")
    public void theMerchantInitiatesAPaymentForKrWithTheCustomerToken(int amount) {
        Transaction transaction = new Transaction(token,registeredMerchant.getUniqueId(), amount, "test1");
        success = merchantAPI.postTransaction(transaction);
    }
}