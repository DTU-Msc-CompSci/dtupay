package org.acme;
//import messaging.implementations.RabbitMqQueue;
import java.util.logging.Logger;


public class StartUp {

	private static final Logger LOGGER = Logger.getLogger("ListenerBean");
	static TokenService tokenService = null;

	public static void main(String[] args) throws Exception {
		LOGGER.info("The Token Service is starting...");
		tokenService = new TokenFactory().getService();
		LOGGER.info("The Token Service has started...");
	}
}
