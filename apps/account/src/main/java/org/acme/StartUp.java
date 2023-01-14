package org.acme;
import messaging.implementations.RabbitMqQueue;
import java.util.logging.Logger;

public class StartUp {
	private static final Logger LOGGER = Logger.getLogger("ListenerBean");
	public static void main(String[] args) throws Exception {
		LOGGER.info("The Account Service is starting...");
		var mq = new RabbitMqQueue("localhost");
		LOGGER.info("The customer Service is starting...11");
		new AccountService(mq);
	}
}
