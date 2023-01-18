package behaviourtests;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class MerchantAPI {

    private final WebTarget baseUrl;

    public MerchantAPI() {
        Client client = ClientBuilder.newClient();
        this.baseUrl = client.target("http://localhost:8091/");

    }

    public DTUPayUser postMerchant(DTUPayUser user) throws Exception {
        Response response = baseUrl.path("merchant")
                .request()
                .post(Entity.entity(user,MediaType.APPLICATION_JSON));

        if (response.getStatus() == 201) {
            return response.readEntity(new GenericType<>() {});
        } else {
            throw new Exception(response.readEntity(String.class));
        }
    }

    public boolean postTransaction(Transaction transaction) {
        Response response = baseUrl.path("merchant/transaction")
                .request()
                .post(Entity.entity(transaction, MediaType.APPLICATION_JSON));
        return response.getStatus() == 201;
    }
}
