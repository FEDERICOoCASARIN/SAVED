package org.saved4.saved4.auth;

import org.saved4.saved4.enums.UserType;

import java.security.Principal;

public class UserPrincipal implements Principal {
    private final String username;
    private final UserType type;

    public UserPrincipal(String username, UserType type) {
        this.username = username;
        this.type = type;
    }

    public UserType getType() {
        return type;
    }

    @Override
    public String getName() {
        return username;
    }
}
