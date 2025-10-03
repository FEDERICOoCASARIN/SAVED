package org.saved4.saved4.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.saved4.saved4.dto.Route;

import java.util.List;

@RolesAllowed("ADMIN")
/**
 * RESTful resource for managing route-related operations.
 * Provides endpoints for retrieving, listing, adding, updating, and deleting routes.
 */
@Path("/routes")
public class RouteResource {

    /**
     * Retrieves a single route by its unique ID.
     *
     * @param routeId The ID of the route to retrieve.
     * @return The {@link Route} object corresponding to the given ID, or {@code null} if not found.
     */
    @Path("/{route_id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Route getRoute(@PathParam("route_id") int routeId) {
        // TODO: Implement actual logic to retrieve a route by ID from the database or service.
        return null;
    }

    /**
     * Retrieves a list of routes, with an optional limit on the number of results.
     *
     * @param limit The maximum number of routes to retrieve. If 0 or negative, all available routes are returned.
     * @return A {@link List} of {@link Route} objects. Returns an empty list if no routes are found.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Route> listRoutes(@QueryParam("limit") int limit) {
        // TODO: Implement actual logic to list routes from the database or service, applying the limit if necessary.
        return null;
    }

    /**
     * Adds a new route to the system.
     *
     * @param route The {@link Route} object to be added.
     * @return The ID of the newly added route, or 0 if the operation fails.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public int addRoute(Route route) {
        // TODO: Implement actual logic to persist the new route to the database.
        return 0;
    }

    /**
     * Updates an existing route identified by its ID.
     * The provided {@link Route} object contains the updated information.
     *
     * @param routeId The ID of the route to update.
     * @param route The {@link Route} object containing the updated data.
     * @return The updated {@link Route} object, or {@code null} if the route is not found or the update fails.
     */
    @Path("/{route_id}")
    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Route updateRoute(@PathParam("route_id") int routeId, Route route) {
        // TODO: Implement actual logic to update the route in the database based on routeId and provided data.
        return null;
    }

    /**
     * Deletes a route from the system by its unique ID.
     *
     * @param routeId The ID of the route to delete.
     */
    @Path("/{route_id}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteRoute(@PathParam("route_id") int routeId) {
        // TODO: Implement actual logic to delete the route from the database.
    }
}
