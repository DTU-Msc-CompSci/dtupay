import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class CustomerAPI {

    private final WebTarget baseUrl;

    public CustomerAPI() {
        Client client = ClientBuilder.newClient();
        this.baseUrl = client.target("http://localhost:8282/");
    }

    public Response postCustomer(DTUPayUser user) {
        return baseUrl.path("customer")
                .request()
                .post(Entity.entity(user,MediaType.APPLICATION_JSON));
    }

    public Response requestToken(DTUPayUser user, int amount) { //Customer requests token
        TokenRequest tokenRequest = new TokenRequest(user, amount);
        return baseUrl.path("token")
                .request()
                .post(Entity.entity(tokenRequest,MediaType.APPLICATION_JSON));
    }
}