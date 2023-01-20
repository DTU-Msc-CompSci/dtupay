package org.acme.repositories;

import messaging.MessageQueue;
import org.acme.aggregate.Payment;

public class PaymentRepository {

    private final EventStore eventStore;

    public PaymentRepository(MessageQueue bus) {
        eventStore = new EventStore(bus);
    }

    public Payment getById(String userId) {
        return Payment.createFromEvents(eventStore.getEventsFor(userId));
    }

    public void save(Payment payment) {
        eventStore.addEvents(payment.getTransactionID(), payment.getAppliedEvents());
        payment.clearAppliedEvents();
    }
}
