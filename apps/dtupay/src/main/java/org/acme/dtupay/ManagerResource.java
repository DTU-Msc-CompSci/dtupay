package org.acme.dtupay;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

@Path("/manager")
public class ManagerResource {

    DTUPayService service = new DTUPayFactory().getService();


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/report")
    public Response getManagerReport( ) {
        Set<TransactionManagerView> reports = service.getManagerReports().getReports();
        if (reports.size() == 0) {
            return Response.status(400).entity("No reports available").build();
        }
        return Response.status(201).entity(reports).build();
    }



}
