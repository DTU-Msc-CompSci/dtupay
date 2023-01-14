package org.acme;


import messaging.Event;
import org.crac.Core;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/customer")
public class CustomerResource {

    CoreService service = new CoreFactory().getService();


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postCustomer(DTUPayUser user) {
        service.registerCustomer(user);

        return Response.status(201).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/token")
    public Response postToken(TokenRequest tokenRequest) {

        // ADD EVENT TO SEND TO TOKEN SERVICE

        return Response.status(201).build();
    }


}
