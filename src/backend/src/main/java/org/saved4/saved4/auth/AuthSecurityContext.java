package org.saved4.saved4.auth;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.SecurityContext;

import java.security.Principal;

public class AuthSecurityContext implements SecurityContext {
    private final UserPrincipal userPrincipal;
    private final ContainerRequestContext requestContext;

    public AuthSecurityContext(UserPrincipal userPrincipal, ContainerRequestContext requestContext) {
        this.userPrincipal = userPrincipal;
        this.requestContext = requestContext;
    }

    @Override
    public Principal getUserPrincipal() {
        return userPrincipal;
    }

    @Override
    public boolean isUserInRole(String s) {
        return userPrincipal.getType().toString().equalsIgnoreCase(s);
    }

    @Override
    public boolean isSecure() {
        return requestContext.getUriInfo().getAbsolutePath().getScheme().equals("https");
    }

    @Override
    public String getAuthenticationScheme() {
        return "Bearer";
    }
}
