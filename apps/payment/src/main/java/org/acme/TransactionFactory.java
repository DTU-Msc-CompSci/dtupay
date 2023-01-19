package org.acme;

import dtu.ws.fastmoney.BankServiceService;
import messaging.implementations.RabbitMqQueue;
import org.acme.repositories.PaymentRepository;
import org.acme.repositories.ReadModelRepository;
import org.acme.service.TransactionService;

public class TransactionFactory {
    static TransactionService transactionService = null;

    public synchronized TransactionService getService() {
        // The singleton pattern.
        // Ensure that there is at most
        // one instance of a PaymentService
        if (transactionService != null) {
            return transactionService;
        }

        // Hookup the classes to send and receive
        // messages via RabbitMq, i.e. RabbitMqSender and
        // RabbitMqListener.
        // This should be done in the factory to avoid
        // the PaymentService knowing about them. This
        // is called dependency injection.
        // At the end, we can use the PaymentService in tests
        // without sending actual messages to RabbitMq.
        var mq = new RabbitMqQueue("rabbitmq");
        var repository = new PaymentRepository(mq);
        var readRepository = new ReadModelRepository(mq);
        var bankService =  new BankServiceService().getBankServicePort();
        transactionService = new TransactionService(mq,bankService,repository,readRepository);

        return transactionService;
    }
}
