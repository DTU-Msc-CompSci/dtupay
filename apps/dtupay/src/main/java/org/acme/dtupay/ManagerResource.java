package org.acme.dtupay;


import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

@Path("/manager")
public class ManagerResource {

    DTUPayService service = new DTUPayFactory().getService();


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Returns all transactions for DTU Pay", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionManagerView.class))),
            @APIResponse(responseCode = "404", description = "No Transactions available for DTU Pay")
    })
    @Path("/report")
    public Response getManagerReport( ) {
        Set<TransactionManagerView> reports = service.getManagerReports().getReports();
        if (reports.size() == 0) {
            return Response.status(404).entity("No reports available").build();
        }
        return Response.status(201).entity(reports).build();
    }



}
