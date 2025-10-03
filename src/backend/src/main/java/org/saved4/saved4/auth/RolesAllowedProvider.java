package org.saved4.saved4.auth;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.ext.Provider;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class RolesAllowedProvider extends RolesAllowedDynamicFeature {
}
