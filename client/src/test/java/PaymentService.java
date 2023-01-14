import messaging.Event;
import messaging.MessageQueue;

import java.util.concurrent.CompletableFuture;

public class PaymentService {
    private MessageQueue queue;
    private CompletableFuture<String> responseCompletableFuture;



    public PaymentService(MessageQueue q) {
        queue = q;
        queue.addHandler("TransactionCompleted", this::handleTransactionCompleted);
        //queue.addHandler("TransactionFailed", this::handleCustomerAccountCreated);

    }

    public String transactionRequest(Transaction t) {
        responseCompletableFuture = new CompletableFuture<>();
        Event event = new Event("TransactionRequested", new Object[] { t });
        queue.publish(event);
        return responseCompletableFuture.join();
    }


    public void handleTransactionCompleted(Event e) {
        var s = e.getArgument(0, String.class);
        System.out.println(s);
        responseCompletableFuture.complete(s);
    }

}
