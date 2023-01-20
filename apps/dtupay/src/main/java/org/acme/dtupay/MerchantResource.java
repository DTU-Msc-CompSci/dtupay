package org.acme.dtupay;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

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
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/deregister")
    public Response deRegisterMerchant(DTUPayUser user) {
        service.deRegisterMerchant(user);
        return Response.ok().build();
    }
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/report/{id}")
    public Response getManagerReport(@PathParam("id") String id ) {
        Set<TransactionUserView> reports = service.getMerchantReports(id).getReports();
        if (reports.size() == 0) {
            return Response.status(400).entity("No reports available").build();
        }
        return Response.status(201).entity(reports).build();
    }


}
