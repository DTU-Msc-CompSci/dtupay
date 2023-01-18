import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;

import org.acme.*;
import org.acme.aggregate.Payment;
import org.acme.repositories.PaymentRepository;

import java.util.*;
import static org.mockito.Mockito.*;


public class ExampleSteps {
    @Given("A naive scenario")
    public void naiveScenario() {

        //tests for debugging need to be removed for final hand in
//         MessageQueue q = mock(MessageQueue.class);
//         //mo
//        String transactionID ="transactionid";
//         String customerBankID="customerBankid";
//         String merchantBankID="merchantbankid";
//         String customerToken ="customertoken";
//         String merchantID="merchantid";
//         BigDecimal amount= BigDecimal.valueOf(2);
//        PaymentRepository pr = new PaymentRepository(q);
//        Payment payment = pr.getById(transactionID);
//        payment.addCustomerBankID(transactionID, customerBankID);
//        payment.addMerchantBankID(transactionID, merchantBankID);
//        payment.create(transactionID, customerToken,merchantID,amount);
//        pr.save(payment);
//        var payment2 = pr.getById(transactionID);
//        var paymentsdfw = payment2;





    }
}
