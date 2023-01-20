package org.acme.dtupay;


import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

@Path("/customer")
public class CustomerResource {

    DTUPayService service = new DTUPayFactory().getService();


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Customer created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DTUPayUser.class))),
            @APIResponse(responseCode = "404", description = "Customer already exists")
    })
    public Response postCustomer(DTUPayUser user) {
        AccountResponse response = service.registerCustomer(user);
        if (!response.getMessage().equals("Success")) {
            return Response.status(404).entity(response.getMessage()).build();
        }
        return Response.status(201).entity(response.getUser()).build();
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Merchant de-registered successfully", content = @Content(mediaType = "application/json")),
    })
    public Response deRegisterCustomer(@PathParam("id") String id) {
        if (service.deRegisterCustomer(id)) {
            return Response.status(204).build();
        } else {
            return Response.status(400).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Attempt to get N tokens for the specified customer", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponse.class))),
            @APIResponse(responseCode = "400", description = "Cannot get the amount of tokens specified"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    @Path("/token")
    public Response postToken(TokenRequest tokenRequest) {
        try {
            TokenResponse response = service.getToken(tokenRequest);
            if (!response.getMessage().equals("Success")) {
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
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Gets the Transactions for the Customer", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionUserView.class))),
            @APIResponse(responseCode = "404", description = "No Transactions available for the provided Customer")
    })
    @Path("/report/{id}")
    public Response getManagerReport(@PathParam("id") String id ) {
        Set<TransactionUserView> reports = service.getCustomerReports(id).getReports();
        for (TransactionUserView d : reports){
            System.out.println(d);
        }
        if (reports.size() == 0) {
            return Response.status(404).entity("No reports available").build();
        }
        return Response.status(201).entity(reports).build();
    }

}
