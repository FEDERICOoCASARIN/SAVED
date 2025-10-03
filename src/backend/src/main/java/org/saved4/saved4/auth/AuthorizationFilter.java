package org.saved4.saved4.auth;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.saved4.saved4.enums.UserType;

import static org.saved4.saved4.auth.AuthenticationFilter.isExcluded;

@Provider
@Priority(Priorities.AUTHORIZATION - 1)
public class AuthorizationFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();

        if (isExcluded(path)) {
            return;
        }

        String username = (String) requestContext.getProperty("username");
        UserType userType = (UserType) requestContext.getProperty("userType");

        UserPrincipal userPrincipal = new UserPrincipal(username, userType);
        AuthSecurityContext securityContext = new AuthSecurityContext(userPrincipal, requestContext);
        requestContext.setSecurityContext(securityContext);
    }
}
