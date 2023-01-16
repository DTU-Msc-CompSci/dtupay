package behaviourtests;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class CustomerAPI {

    private final WebTarget baseUrl;

    public CustomerAPI() {
        Client client = ClientBuilder.newClient();
        this.baseUrl = client.target("http://localhost:8091/");
    }

    public DTUPayUser postCustomer(DTUPayUser user) {

        Response response = baseUrl.path("customer")
                .request()
                .post(Entity.entity(user,MediaType.APPLICATION_JSON));
        return response.readEntity(DTUPayUser.class);

    }

    public Token requestToken(String cid, int amount) { //Customer requests token
        TokenRequest tokenRequest = new TokenRequest(cid, amount);
        Response response = baseUrl.path("customer/token")
                            .request()
                            .post(Entity.entity(tokenRequest,MediaType.APPLICATION_JSON));
        return response.readEntity(Token.class);
    }
}