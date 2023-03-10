package org.acme;
//import messaging.implementations.RabbitMqQueue;

import java.util.logging.Logger;

public class StartUp {

    static AccountService accountService = null;
    private static final Logger LOGGER = Logger.getLogger("ListenerBean");

    public static void main(String[] args) throws Exception {
        LOGGER.info("The Account Service is starting...");
        accountService = new AccountServiceFactory().getService();
        LOGGER.info("The Account Service has started...");
    }
}
