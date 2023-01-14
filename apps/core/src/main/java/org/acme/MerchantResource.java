package org.acme;

import jakarta.transaction.Transaction;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;

@Path("/merchant")
public class MerchantResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postCustomer(DTUPayUser user) {

        return Response.status(201).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/transaction")
    public Response postTransaction(Transaction transaction) {

        // SEND EVENT TO TRANSACTION (PAYMENT?) SERVICE

        return Response.status(201).build();
    }
}
