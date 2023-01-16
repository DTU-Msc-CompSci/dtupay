package org.acme;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

// This concept needs to be added to ALL other microservices
@ApplicationScoped
public class TokenStartUp {

	private static final Logger LOGGER = Logger.getLogger("ListenerBean");
	TokenService tokenService = null;

	void onStart(@Observes StartupEvent event) {
		LOGGER.info("The Token Service is starting...");
		tokenService = new TokenFactory().getService();
		LOGGER.info("The Token Service has started...");
	}

	void onStop(@Observes ShutdownEvent event) {
		LOGGER.info("The Token Service is stopping...");
	}
}
