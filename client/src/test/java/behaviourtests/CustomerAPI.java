package behaviourtests;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomerAPI {

    private final WebTarget baseUrl;

    public CustomerAPI() {
        Client client = ClientBuilder.newClient();
        this.baseUrl = client.target("http://localhost:8080/");
    }

    public Response deregisterCustomer(DTUPayUser user) throws Exception {
        Response response = baseUrl.path("customer/deregister")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(user, MediaType.APPLICATION_JSON));
        if (response.getStatus() == 200 || response.getStatus() == 201) {
            return response;
        } else {
            throw new Exception(response.readEntity(String.class));
        }
    }

    public DTUPayUser postCustomer(DTUPayUser user) throws Exception {

        try (Response response = baseUrl.path("customer")
                .request()
                .post(Entity.entity(user, MediaType.APPLICATION_JSON))) {

            if (response.getStatus() == 200 || response.getStatus() == 201) {
                return response.readEntity(DTUPayUser.class);
            } else {
                throw new Exception(response.readEntity(String.class));
            }
        }
    }

    public Set<Token> requestToken(String cid, int amount) throws Exception {
        TokenRequest tokenRequest = new TokenRequest(cid, amount);
        System.out.println(tokenRequest);
        try (Response response = baseUrl.path("customer/token")
                .request()
                .post(Entity.entity(tokenRequest, MediaType.APPLICATION_JSON))) {
            System.out.println(response.getStatus());
            if (response.getStatus() == 200 || response.getStatus() == 201) {
                return response.readEntity(new GenericType<>() {
                });
            } else {
                throw new Exception(response.readEntity(String.class));
            }
        }
    }
    public Set<TransactionUserView> getReport(String id) throws Exception {
        Response response = baseUrl.path("customer/report/"+id)
                .request(MediaType.APPLICATION_JSON)
                .get()
                ;
        if (response.getStatus() == 200 || response.getStatus() == 201) {

            return response.readEntity(new GenericType<>() {});
        } else {
            throw new Exception(response.readEntity(String.class));
        }
    }
}