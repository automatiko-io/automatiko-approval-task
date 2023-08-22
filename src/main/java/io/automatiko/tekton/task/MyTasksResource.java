package io.automatiko.tekton.task;

import java.util.List;
import java.util.stream.Collectors;

import io.automatiko.engine.api.auth.IdentityProvider;
import io.automatiko.engine.api.auth.IdentitySupplier;
import io.quarkus.qute.Engine;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/mytasks")
public class MyTasksResource {

    private IdentitySupplier identitySupplier;

    private Engine engine;

    @Inject
    public MyTasksResource(IdentitySupplier identitySupplier, Engine engine) {
        this.identitySupplier = identitySupplier;
        this.engine = engine;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@QueryParam("user") final String user,
            @QueryParam("group") final List<String> groups) {

        IdentityProvider identityProvider = identitySupplier.buildIdentityProvider(user, groups);
        try {
            TemplateInstance templateInstance = engine.getTemplate("my-tasks.html").data("user", identityProvider.getName(),
                    "groups", identityProvider.getRoles().stream().collect(Collectors.joining("&group=")));

            return templateInstance;
        } finally {
            IdentityProvider.set(null);
        }
    }
}
