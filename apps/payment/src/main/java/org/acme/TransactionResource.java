package org.acme;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

@Path("/transaction")
public class TransactionResource {

    private TransactionService transactionService = new TransactionService();


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response initiateTransaction(Transaction transaction) {
        transactionService.initiateTransaction(transaction);
        return Response.created(URI.create("exampleTransaction")).build();
    }
}
