package org.saved4.saved4.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import org.saved4.saved4.dto.Company;
import org.saved4.saved4.dto.User;
import org.saved4.saved4.enums.UserType;
import org.saved4.saved4.service.CompaniesService;
import org.saved4.saved4.service.UserService;

@RolesAllowed({"ADMIN", "COMPANY"})
@Path("/companies")
public class CompanyResource {
    @Context
    private ContainerRequestContext requestContext;
    @Inject
    private CompaniesService companiesService;
    @Inject
    private UserService userService;

    @Path("/{username}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Company getCompany(@PathParam("username") String username) {
        //        if (!requestContext.getProperty("username").equals(username)) {
        //            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        //        }
        try {
            Company result = companiesService.getCompanyByUsername(username);
            return result;
        } catch (Exception e) {
            throw new WebApplicationException("Failed to retrieve company: " + e.getMessage(),
                                              jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @RolesAllowed("ADMIN")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Company> listCompanies() {
        try {
            List<Company> companies = companiesService.getAllCompanies();
            return companies;
        } catch (Exception e) {
            e.printStackTrace();

            throw new WebApplicationException("Failed to retrieve companies: " + e.getMessage(),
                                              jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @RolesAllowed("ADMIN")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response addCompany(Company company) {
        URI result = companiesService.createCompany(company);
        if (result == null) {
            return Response.serverError().build();
        }
        return Response.created(result).build();
    }


    @Path("/{username}")
    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateCompany(@PathParam("username") String username, Company company) {
        try {
            // Get current user info from request context
            UserType currentUserType = (UserType) requestContext.getProperty("userType");

            // Get the target user's information
            User targetUser = userService.getUserByUsername(username);
            if (targetUser == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            }

            // Prevent admins from editing other admins
            if (currentUserType == UserType.ADMIN && targetUser.getUserType() == UserType.ADMIN) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("Admins cannot edit other admin users").build();
            }

            companiesService.updateCompany(username, company);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error updating company: " + e.getMessage()).build();
        }
    }

    @Path("/{username}")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteCompany(@PathParam("username") String username) {
        companiesService.deleteCompany(username);
        return Response.serverError().build();
    }
}
