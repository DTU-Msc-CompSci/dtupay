package org.acme;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

// This concept needs to be added to ALL other microservices

@ApplicationScoped
public class StartUp {

	private static final Logger LOGGER = Logger.getLogger("ListenerBean");
	AccountService accountService = null;

	void onStart(@Observes StartupEvent event) {
		LOGGER.info("The Account Service is starting...");
		accountService = new AccountServiceFactory().getService();
	}

	void onStop(@Observes ShutdownEvent event) {
		LOGGER.info("The Account Service is stopping...");
	}

}
