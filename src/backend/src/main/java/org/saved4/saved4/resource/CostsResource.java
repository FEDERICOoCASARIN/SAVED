package org.saved4.saved4.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.saved4.saved4.dto.Costs;
import org.saved4.saved4.service.CostsService;

@Path("/costs")
public class CostsResource {
    @Inject
    CostsService costsService;

    @RolesAllowed({"ADMIN", "COMPANY"})
    @GET
    public Response getCosts() {
        Costs costs = costsService.getCosts();
        if (costs == null) {
            return Response.serverError().build();
        }
        return Response.ok(costs).build();
    }

    @RolesAllowed("ADMIN")
    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateCosts(Costs costs) {
        costsService.updateCosts(costs);
        return Response.noContent().build();
    }
}
