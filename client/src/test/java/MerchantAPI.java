import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class MerchantAPI {

    private final WebTarget baseUrl;

    public MerchantAPI() {
        Client client = ClientBuilder.newClient();
        this.baseUrl = client.target("http://localhost:8282/");

    }

    public Response postMerchant(DTUPayUser user) {
        return baseUrl.path("merchant")
                .request()
                .post(Entity.entity(user, MediaType.APPLICATION_JSON));
    }

    public Response postTransaction(DTUPayUser user, Token token, int amount) {
        Transaction transaction = new Transaction(user, token, amount);
        return baseUrl.path("merchant/transaction")
                .request()
                .post(Entity.entity(transaction, MediaType.APPLICATION_JSON));
    }
}
