package org.acme;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import messaging.implementations.RabbitMqQueue;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

// This concept needs to be added to ALL other microservices
@ApplicationScoped
public class StartUp {

	private static final Logger LOGGER = Logger.getLogger("ListenerBean");

	void onStart(@Observes StartupEvent event) {
		LOGGER.info("The Account Service is starting...");
		var mq = new RabbitMqQueue("localhost");
		LOGGER.info("The customer Service is starting...11");

		new TransactionService(mq);



	}

	void onStop(@Observes ShutdownEvent event) {
		LOGGER.info("The Account Service is stopping...");
	}

//	public static void main(String[] args) throws Exception {
//		new StartUp().startUp();
//	}
//
//	private void startUp() throws Exception {
//		System.out.println("CustomerService startup");
//		var mq = new RabbitMqQueue("rabbitMq");
//		new CustomerService(mq);
//		// merchant
//	}
}
