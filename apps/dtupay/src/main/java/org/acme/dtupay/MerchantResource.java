package org.acme.dtupay;


import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

@Path("/merchant")
public class MerchantResource {

    DTUPayService service = new DTUPayFactory().getService();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Merchant created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DTUPayUser.class))),
            @APIResponse(responseCode = "400", description = "Merchant already exists")
    })
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
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Transaction successful", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @APIResponse(responseCode = "400", description = "Transaction failed")
    })
    public Response postTransaction(Transaction transaction) {
        String transactionResult = service.requestTransaction(transaction);
        if (!transactionResult.equals("Success")) {
            return Response.status(400).entity(transactionResult).build();
        }
        return Response.status(201).entity(transactionResult).build();
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Merchant de-registered successfully", content = @Content(mediaType = "application/json")),
    })
    public Response deRegisterMerchant(@PathParam("id") String id) {
        if (service.deRegisterMerchant(id)) {
            return Response.status(204).build();
        } else {
            return Response.status(400).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/report/{id}")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Returns all transactions for Merchant", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionUserView.class))),
            @APIResponse(responseCode = "404", description = "No Transactions available for Merchant")
    })
    public Response getManagerReport(@PathParam("id") String id ) {
        Set<TransactionUserView> reports = service.getMerchantReports(id).getReports();
        if (reports.size() == 0) {
            return Response.status(400).entity("No reports available").build();
        }
        return Response.status(201).entity(reports).build();
    }


}
