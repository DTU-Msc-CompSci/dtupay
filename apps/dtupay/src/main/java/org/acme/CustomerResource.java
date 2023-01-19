package org.acme;


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
        service.deRegisterCustomer(user);
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/token")
    public Response postToken(TokenRequest tokenRequest) {
        TokenResponse response = service.getToken(tokenRequest);
        if (!response.getMessage().equals("success")) {
            return Response.status(400).entity(response.getMessage()).build();
        }
        return Response.status(201).entity(response.getTokens()).build();
    }


}
