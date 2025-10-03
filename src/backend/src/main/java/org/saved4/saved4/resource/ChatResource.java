package org.saved4.saved4.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.saved4.saved4.dto.chat.AdminChat;
import org.saved4.saved4.dto.chat.Chat;
import org.saved4.saved4.dto.chat.CompanyChat;
import org.saved4.saved4.enums.Role;
import org.saved4.saved4.service.ChatsService;

import java.util.List;

@Path("/chats")
public class ChatResource {
    @Context
    private ContainerRequestContext requestContext;
    @Inject
    private ChatsService chatsService;

    @RolesAllowed({"ADMIN", "COMPANY"})
    @Path("/{company}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChat(@PathParam("company") String company) {
        Role userRole = (Role) requestContext.getProperty("db_role");
        String username = (String) requestContext.getProperty("username");

        if (userRole == Role.ADMIN) {
            // Admin requesting specific company chat
            Chat chat = chatsService.getChatByCompany(company);
            return Response.ok(chat).build();
        } else {
            // Company user requesting their own chat with admin
            CompanyChat chat = chatsService.getCompanyChatWithAdmin(username);
            return Response.ok(chat).build();
        }
    }

    @RolesAllowed({"ADMIN", "COMPANY"})
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listChats() {
        Role userRole = (Role) requestContext.getProperty("db_role");
        String username = (String) requestContext.getProperty("username");

        if (userRole == Role.ADMIN) {
            // Admin can see all chats
            List<AdminChat> chats = chatsService.listAllChats();
            return Response.ok(chats).build();
        } else {
            // Company users only see their own chat with admin
            CompanyChat companyChatWithAdmin = chatsService.getCompanyChatWithAdmin(username);
            if (companyChatWithAdmin != null) {
                // Return single chat as list for consistency with frontend
                return Response.ok(List.of(companyChatWithAdmin)).build();
            } else {
                return Response.ok(List.of()).build();
            }
        }
    }
}
