package org.acme;

import messaging.implementations.RabbitMqQueue;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.jboss.logging.Logger;

// This concept needs to be added to ALL other microservices
@ApplicationScoped
public class StartUp {

	private static final Logger LOGGER = Logger.getLogger("ListenerBean");

	void onStart(@Observes StartupEvent event) {
		LOGGER.info("The Token Service is starting...");
		var mq = new RabbitMqQueue("localhost");
		LOGGER.info("The Token Service is starting...11");

		new TokenService(mq);



	}

	void onStop(@Observes ShutdownEvent event) {
		LOGGER.info("The Token Service is stopping...");
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
