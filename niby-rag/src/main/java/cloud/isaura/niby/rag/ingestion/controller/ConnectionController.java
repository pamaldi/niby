package cloud.isaura.niby.rag.ingestion.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Path("/niby/rag")
public class ConnectionController
{

    private static final Logger log = LoggerFactory.getLogger(ConnectionController.class);

    @Inject
    DataSource dataSource;

    @GET
    @Path("/connection")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkConnection() {
        Map<String, Object> response = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(5); // 5 second timeout

            if (isValid) {
                response.put("status", "connected");
                response.put("message", "Datasource is readable and connected");
                response.put("databaseProduct", connection.getMetaData().getDatabaseProductName());
                response.put("databaseVersion", connection.getMetaData().getDatabaseProductVersion());

                log.info("Datasource connection check successful");
                return Response.ok(response).build();
            } else {
                response.put("status", "error");
                response.put("message", "Datasource connection is not valid");

                log.warn("Datasource connection check failed - connection not valid");
                return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(response).build();
            }

        } catch (SQLException e) {
            response.put("status", "error");
            response.put("message", "Failed to connect to datasource: " + e.getMessage());

            log.error("Datasource connection check failed", e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(response).build();
        }
    }
}
