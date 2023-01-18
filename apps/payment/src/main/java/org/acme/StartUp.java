package org.acme;
//import messaging.implementations.RabbitMqQueue;
import org.acme.service.TransactionService;

import java.util.logging.Logger;

public class StartUp {

	private static final Logger LOGGER = Logger.getLogger("ListenerBean");
	static TransactionService transactionService = null;

	public static void main(String[] args) throws Exception {
		LOGGER.info("The Payment Service is starting...");
		transactionService = new TransactionFactory().getService();
	}
}
