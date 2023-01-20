package behaviourtests;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashSet;
import java.util.Set;

public class MerchantAPI {

    private final WebTarget baseUrl;

    public MerchantAPI() {
        Client client = ClientBuilder.newClient();
        this.baseUrl = client.target("http://localhost:8080/");

    }

    public Response deregisterMerchant(DTUPayUser user) {
        Response response = baseUrl.path("merchant/deregister")
                .request()
                .post(Entity.entity(user, MediaType.APPLICATION_JSON));
        if (response.getStatus() == 200 || response.getStatus() == 201) {
            return response;
        } else {
            return null;
        }
    }

    public DTUPayUser postMerchant(DTUPayUser user) throws Exception {
        try (Response response = baseUrl.path("merchant")
                .request()
                .post(Entity.entity(user, MediaType.APPLICATION_JSON))) {

            if (response.getStatus() == 200 || response.getStatus() == 201) {
                return response.readEntity(new GenericType<>() {
                });
            } else {
                throw new Exception(response.readEntity(String.class));
            }
        }
    }

    public boolean postTransaction(Transaction transaction) throws Exception {
        try (Response response = baseUrl.path("merchant/transaction")
                .request()
                .post(Entity.entity(transaction, MediaType.APPLICATION_JSON))) {
            if (response.getStatus() == 200 || response.getStatus() == 201) {
                return response.getStatus() == 201 || response.getStatus() != 200;
            } else {
                throw new Exception("Transaction failed");
            }
        }

    }
    public Set<TransactionUserView> getReport(String id) throws Exception {
        Response response = baseUrl.path("merchant/report/"+id)
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
