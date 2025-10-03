package org.saved4.saved4.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.saved4.saved4.dto.Message;
import org.saved4.saved4.service.MessagesService;

import java.net.URI;
import java.util.List;

@RolesAllowed({"ADMIN", "COMPANY"})
@Path("/messages")
public class MessageResource {

    @Inject
    MessagesService messagesService;

    @Path("/{company}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listMessages(@PathParam("company") String company) {
        List<Message> messages = messagesService.listMessages(company);
        if (messages == null) {
            return Response.serverError().build();
        }
        return Response.ok(messages).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createMessage(Message message) {
        URI uri = messagesService.createMessage(message.getCompany(), message.getBody());
        return Response.created(uri).build();
    }

}
