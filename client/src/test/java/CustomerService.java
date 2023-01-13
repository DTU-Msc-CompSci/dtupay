import messaging.Event;
import messaging.MessageQueue;

import java.util.concurrent.CompletableFuture;

public class CustomerService {
        private MessageQueue queue;
        private CompletableFuture<DTUPayUser> dtuPayUserCompletableFuture;

        public CustomerService(MessageQueue q) {
            queue = q;
            queue.addHandler("CustomerAccountCreated", this::handleCustomerAccountCreated);
        }

        public DTUPayUser register(DTUPayUser d) {
            dtuPayUserCompletableFuture = new CompletableFuture<>();
            Event event = new Event("CustomerAccountCreationRequested", new Object[] { d });
            queue.publish(event);
            return dtuPayUserCompletableFuture.join();
        }

        public void handleCustomerAccountCreated(Event e) {
            var s = e.getArgument(0, DTUPayUser.class);
            System.out.println(s.toString());
            dtuPayUserCompletableFuture.complete(s);
        }
}
