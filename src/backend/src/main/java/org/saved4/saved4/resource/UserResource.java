package org.saved4.saved4.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.saved4.saved4.dto.User;
import org.saved4.saved4.enums.UserType;
import org.saved4.saved4.service.CompaniesService;
import org.saved4.saved4.service.UserService;

@Path("/users")
public class UserResource {

    @Inject
    private UserService userService;

    @Inject
    private CompaniesService companiesService;

    @Context
    private ContainerRequestContext requestContext;

    @Path("/{username}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser(@PathParam("username") String username) {
        try {
            User result = userService.getUserByUsername(username);
            System.out.println("Successfully retrieved");
            return result;
        } catch (Exception e) {
            System.err.println("Error calling CompanyService.getCompany() from UserResource:");
            e.printStackTrace();

            throw new WebApplicationException("Failed to retrieve company: " + e.getMessage(),
                                              jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> listUsers(@QueryParam("limit") int limit, @QueryParam("type") String type) {
        try {
            List<User> users = userService.getAllUsers(type);
            if (users != null) {
                System.out.println(
                        "Successfully retrieved users from service. Count: " + users.size() +
                                " Type filter: " + type);
            }
            return users;
        } catch (Exception e) {
            System.err.println("Error calling UserService.getAllUsers() from UserResource:");
            e.printStackTrace();

            throw new WebApplicationException("Failed to retrieve users: " + e.getMessage(),
                                              jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Path("/{username}")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteUser(@PathParam("username") String username) {
        // Get current user info from request context
        UserType currentUserType = (UserType) requestContext.getProperty("userType");

        // Get the target user's information
        User targetUser = userService.getUserByUsername(username);
        if (targetUser == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
        }

        // Prevent admins from deleting other admins
        if (currentUserType == UserType.ADMIN && targetUser.getUserType() == UserType.ADMIN) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Admins cannot delete other admin users").build();
        }

        companiesService.deleteCompany(username);
        return Response.ok().build();
    }
}
