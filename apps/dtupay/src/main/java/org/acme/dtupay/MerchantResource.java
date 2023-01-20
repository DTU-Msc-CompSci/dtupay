package org.acme.dtupay;

//import jakarta.transaction.Transaction;
//import jakarta.ws.rs.Path;
//import jakarta.ws.rs.core.MediaType;
//import jakarta.ws.rs.core.Response;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/merchant")
public class MerchantResource {

    CoreService service = new CoreFactory().getService();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postMerchant(DTUPayUser user) {
        AccountResponse response = service.registerMerchant(user);
        if (!response.getMessage().equals("Success")) {
            return Response.status(400).entity(response.getMessage()).build();
        }
        return Response.status(201).entity(response.getUser()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/transaction")
    public Response postTransaction(Transaction transaction) {
        String transactionResult = service.requestTransaction(transaction);
        if (!transactionResult.equals("Success")) {
            return Response.status(400).entity(transactionResult).build();
        }
        return Response.status(201).entity(transactionResult).build();

//        return service.requestTransaction(transaction);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/deregister")
    public Response deRegisterMerchant(DTUPayUser user) {
        service.deRegisterMerchant(user);
        return Response.ok().build();
    }


}