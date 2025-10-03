package org.saved4.saved4.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.saved4.saved4.dto.LoginRequest;
import org.saved4.saved4.dto.PasswordResetRequest;
import org.saved4.saved4.service.AuthService;

@Path("/auth")
public class AuthResource {

    @Inject
    AuthService authService;

    @Path("/login")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest request) {
        System.out.println("hit");
        return authService.login(request);
    }

    @Path("/logout")
    @GET
    public Response logout() {
        NewCookie expiredCookie =
                new NewCookie.Builder("token").value("").path("/").httpOnly(true).secure(true)
                        .maxAge(0).sameSite(NewCookie.SameSite.STRICT).build();

        return Response.ok("Logged out").cookie(expiredCookie).build();
    }

    @Path("/password")
    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetPassword(@Context ContainerRequestContext requestContext, PasswordResetRequest request) {
        return authService.resetPassword(requestContext, request);
    }
}
