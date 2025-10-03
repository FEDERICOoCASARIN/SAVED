package org.saved4.saved4.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.saved4.saved4.dto.User;
import org.saved4.saved4.service.RegisterService;

import java.net.URI;

@Path("/register")
public class RegisterResource {
    @Inject
    RegisterService registerService;

    @RolesAllowed("ADMIN")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response requestToken(User user) {
        String token = registerService.requestToken(user);
        return Response.ok(token).build();
    }

    @Path("/{token}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(@PathParam("token") String token, User user) {
        System.out.println(user.getUserType());
        URI uri = registerService.registerUser(token, user);
        return Response.created(uri).build();
    }

    @Path("/validate/{token}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateToken(@PathParam("token") String token) {
        Object companyInfo = registerService.validateToken(token);
        return Response.ok(companyInfo).build();
    }
}
