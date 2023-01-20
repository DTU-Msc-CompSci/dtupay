package org.acme.dtupay;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

@Path("/customer")
public class CustomerResource {

    CoreService service = new CoreFactory().getService();


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postCustomer(DTUPayUser user) {
        AccountResponse response = service.registerCustomer(user);
        if (!response.getMessage().equals("Success")) {
            return Response.status(400).entity(response.getMessage()).build();
        }
        return Response.status(201).entity(response.getUser()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/deregister")
    public Response deRegisterCustomer(DTUPayUser user) {
        if (service.deRegisterCustomer(user)) {
            System.out.println("Customer de-registered");
            return Response.status(200).entity("Success").build();
        } else {
            System.out.println("Customer de-registration failed");
            return Response.status(400).entity("Account does not exist in DTUPay").build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/token")
    public Response postToken(TokenRequest tokenRequest) {
        try {
            TokenResponse response = service.getToken(tokenRequest);
            if (!response.getMessage().equals("success")) {
                return Response.status(400).entity(response.getMessage()).build();
            }
            return Response.status(201).entity(response.getTokens()).build();
        } catch (Exception e) {
            System.out.println("CompletableFuture timeout.");
            System.out.println(e.getMessage());
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/report/{id}")
    public Response getManagerReport(@PathParam("id") String id ) {
        Set<TransactionUserView> reports = service.getCustomerReports(id).getReports();
        for (TransactionUserView d : reports){
            System.out.println(d);
        }
        if (reports.size() == 0) {
            return Response.status(400).entity("No reports available").build();
        }
        return Response.status(201).entity(reports).build();
    }

}
