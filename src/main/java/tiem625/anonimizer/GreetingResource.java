package tiem625.anonimizer;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jooq.DSLContext;

@Path("/hello")
public class GreetingResource {

    @Inject
    DSLContext dslContext;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Mysql connection OK: " + dslContext.configuration();
    }
}
