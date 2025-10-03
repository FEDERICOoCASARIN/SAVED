package org.saved4.saved4.exceptions;

import jakarta.ws.rs.WebApplicationException;

public class ConflictException extends WebApplicationException {

    public ConflictException() {
        super("Resource already exists", 409);
    }
}
