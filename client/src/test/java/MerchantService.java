import messaging.Event;
import messaging.MessageQueue;

import java.util.concurrent.CompletableFuture;

public class MerchantService {
    private MessageQueue queue;
    private CompletableFuture<DTUPayUser> dtuPayUserCompletableFuture;


    public MerchantService(MessageQueue q) {
        queue = q;
        queue.addHandler("MerchantAccountCreated", this::handleMerchantAccountCreated);

    }


    public DTUPayUser registerMerchant(DTUPayUser d) {
        dtuPayUserCompletableFuture = new CompletableFuture<>();
        Event event = new Event("MerchantAccountCreationRequested", new Object[] { d });
        queue.publish(event);
        return dtuPayUserCompletableFuture.join();
    }


    public void handleMerchantAccountCreated(Event e) {
        var s = e.getArgument(0, DTUPayUser.class);
        System.out.println(s.toString());
        dtuPayUserCompletableFuture.complete(s);
    }
}
