package tiem625;

import io.agroal.api.AgroalDataSource;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.sql.SQLException;

@Path("/hello")
public class GreetingResource {

    @Inject
    private AgroalDataSource dataSource;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() throws SQLException {
        return "Mysql connection OK: " + dataSource.isHealthy(true);
    }
}
