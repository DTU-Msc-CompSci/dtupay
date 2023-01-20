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
import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

public class ReportingStepsTest {
    BankService bankService = new BankServiceService().getBankServicePort();
    private CustomerAPI customerAPI = new CustomerAPI();
    private MerchantAPI merchantAPI = new MerchantAPI();
    private ManagerAPI managerAPI = new ManagerAPI();


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
    String error;

    Set<TransactionManagerView> managerReport;
    Set<TransactionUserView> customerReport;
    Set<TransactionUserView> merchantReport;



    @Before
    public void init() throws Exception {
        customer.setFirstName("Saldvatore");
        customer.setLastName("Koondce");
        customer.setCprNumber("2806d91-3459");
        customerBankId = bankService.createAccountWithBalance(customer, BigDecimal.valueOf(20000));
        dtuPayCustomer.setBankId(new BankId(customerBankId));
        dtuPayCustomer.setPerson(new Person(customer.getFirstName(),customer.getLastName(),customer.getCprNumber()));
        registeredCustomer = customerAPI.postCustomer(dtuPayCustomer);

        merchant.setFirstName("Alfdonso");
        merchant.setLastName("Quardles");
        merchant.setCprNumber("1101d48-2517");
        merchantBankId = bankService.createAccountWithBalance(merchant, BigDecimal.valueOf(10000));
        dtuPayMerchant.setBankId(new BankId(merchantBankId));
        dtuPayMerchant.setPerson(new Person(merchant.getFirstName(),merchant.getLastName(),merchant.getCprNumber()));
        registeredMerchant = merchantAPI.postMerchant(dtuPayMerchant);

        Set<Token> tokens = customerAPI.requestToken(registeredCustomer.getUniqueId(),1);
        token = tokens.iterator().next();


        Transaction transaction = new Transaction(token,registeredMerchant.getUniqueId(), 1000, "testReport");
        success = merchantAPI.postTransaction(transaction);

    }

    @After
    public void tearDown() {
        try {
            bankService.retireAccount(customerBankId);
        } catch (BankServiceException_Exception e) {
        }
        try {
            bankService.retireAccount(merchantBankId);
        } catch (BankServiceException_Exception e) {
        }
    }
    @Given("a successful transaction")
    public void a_successful_transaction() {
        assertTrue(success);
    }

    @When("the merchant, the customer and the manager ask for a report")
    public void the_merchant_the_customer_and_the_manager_ask_for_a_report() throws Exception {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        managerReport= managerAPI.getReport();
        customerReport= customerAPI.getReport(registeredCustomer.getUniqueId());

        merchantReport= merchantAPI.getReport(registeredMerchant.getUniqueId());


    }

    @Then("the transaction is in the report")
    public void the_transaction_is_in_the_report() {
        assertTrue(1 == managerReport.stream().filter((transactionManagerView -> transactionManagerView.getCustomer() != null
                && transactionManagerView.getCustomer().equals(registeredCustomer)
                && transactionManagerView.getMerchant() != null
                && transactionManagerView.getCustomerToken() != null
                &&  transactionManagerView.getMerchant().equals(registeredMerchant)
                &&  transactionManagerView.getCustomerToken().equals(token.getToken()))).collect(Collectors.toSet()).size());
        assertTrue(1 ==  customerReport.stream().filter((transaction ->
                  transaction.getMerchant() != null &&transaction.getCustomerToken()!= null &&
                transaction.getMerchant().equals(registeredMerchant)
                &&  transaction.getCustomerToken().equals(token.getToken()))).collect(Collectors.toSet()).size());
        assertTrue(1 == merchantReport.stream().filter((transaction -> transaction.getMerchant().equals(registeredMerchant)
                && transaction.getMerchant() != null &&transaction.getCustomerToken()!= null

                &&  transaction.getCustomerToken().equals(token.getToken()))).collect(Collectors.toSet()).size());

    }
}