package org.acme;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
    @Path("/deregister")
    public Response deRegisterCustomer(DTUPayUser user) {
        service.deRegisterCustomer(user);
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/token")
    public Token postToken(TokenRequest tokenRequest) {
        System.out.println(tokenRequest.getCid());
        return service.getToken(tokenRequest);
    }


}
