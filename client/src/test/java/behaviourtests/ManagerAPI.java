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

public class ManagerAPI {

    private final WebTarget baseUrl;

    public ManagerAPI() {
        Client client = ClientBuilder.newClient();
        this.baseUrl = client.target("http://localhost:8080/");
    }

    // deregister customer with given id
    public Set<TransactionManagerView> getReport() throws Exception {
        Response response = baseUrl.path("manager/report")
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