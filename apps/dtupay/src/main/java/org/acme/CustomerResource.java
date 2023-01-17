package org.acme;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

@Path("/customer")
public class CustomerResource {

    CoreService service = new CoreFactory().getService();


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DTUPayUser postCustomer(DTUPayUser user) {
        return service.registerCustomer(user);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/token")
    public Response postToken(TokenRequest tokenRequest) {
        var response = service.getToken(tokenRequest);

        if (response instanceof Set<?>) {
            return Response.status(201).entity(response).build();
        } else {
            return Response.status(400).entity(response).build();
        }
    }


}
