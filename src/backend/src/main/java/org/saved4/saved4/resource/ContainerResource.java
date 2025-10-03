package org.saved4.saved4.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.postgresql.util.PGTimestamp;
import org.saved4.saved4.dto.Container;
import org.saved4.saved4.service.ContainerService;

import java.net.URI;
import java.util.List;

@RolesAllowed("ADMIN")
@Path("/containers")
public class ContainerResource {
    @Inject
    ContainerService containerService;

    /**
     * Retrieves a single container by its unique ID.
     *
     * @param containerId The ID of the container to retrieve.
     * @return The {@link Container} object corresponding to the given ID.
     */
    @Path("/{container_id}")
    @GET
    public Response getContainer(@PathParam("container_id") int containerId) {
        Container container = containerService.getContainerById(containerId);
        return Response.ok(container).build();
    }

    /**
     * Retrieves a list of all containers, with an optional limit on the number of results.
     * Defaults to 100 containers if no limit is specified.
     *
     * @param limit The maximum number of containers to retrieve.
     * @return A {@link List} of {@link Container} objects. Returns an empty list if no containers are found.
     */
    @GET
    public Response listContainers(@QueryParam("at_company") String atCompany, @QueryParam("tw_start") long twStart, @QueryParam("tw_end") long twEnd) {
        List<Container> containers = containerService.listContainers(atCompany, new PGTimestamp(twStart), new PGTimestamp(twEnd));
        return Response.ok(containers).build();
    }

    /**
     * Adds a new container to the system.
     *
     * @param container The {@link Container} object to be added.
     * @return Response with the URI of the newly created container.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addContainer(Container container) {
        URI containerUri = containerService.createContainer(container.getContainerId(), container.getMaxWeight());
        return Response.created(containerUri).build();
    }

    /**
     * Deletes a container from the system by its unique ID.
     *
     * @param containerId The ID of the container to delete.
     */
    @Path("/{container_id}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteContainer(@PathParam("container_id") int containerId) {
        containerService.deleteContainer(containerId);
    }

    /**
     * Updates an existing container identified by its ID.
     * Only `maxWeight` and `status` fields are considered for updates from the provided container object.
     *
     * @param containerId The ID of the container to update.
     * @param container The {@link Container} object containing the updated data.
     * @return The updated {@link Container} object.
     * @throws WebApplicationException if the container is not found.
     */
    @Path("/{container_id}")
    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Container updateContainer(@PathParam("container_id") int containerId, Container container) {
        // Retrieve the current container details to apply partial updates.
        Container currentContainer = containerService.getContainerById(containerId);
        if (currentContainer == null) {
            // Throw a 404 Not Found exception if the container doesn't exist.
            throw new WebApplicationException("Container not found with id " + containerId, jakarta.ws.rs.core.Response.Status.NOT_FOUND);
        }

        // Apply updates only if the corresponding fields are provided in the request.
        if (container.getMaxWeight() != null) {
            currentContainer.setMaxWeight(container.getMaxWeight());
        }

        if (container.getContainerStatus() != null) {
            currentContainer.setContainerStatus(container.getContainerStatus());
        }

        // Set the ID to ensure the update operation targets the correct container.
        currentContainer.setContainerId(containerId);

        // Call the service layer to perform the update.
        containerService.updateContainer(containerId, currentContainer);
        
        // Return the updated container
        return currentContainer;
    }
}
