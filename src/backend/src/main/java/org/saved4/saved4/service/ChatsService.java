package org.saved4.saved4.service;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import org.saved4.saved4.dao.ChatDao;
import org.saved4.saved4.dto.chat.AdminChat;
import org.saved4.saved4.dto.chat.Chat;
import org.saved4.saved4.dto.chat.CompanyChat;
import org.saved4.saved4.enums.Role;
import org.saved4.saved4.exceptions.ConflictException;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;

@RequestScoped
public class ChatsService {
    @Inject
    private ChatDao chatDao;

    @Context
    private ContainerRequestContext requestContext;
    @Context
    private UriInfo uriInfo;

    public URI createChat(Chat chat) {
        String chatCompany;
        try {
            chatCompany = chatDao.create((Role) requestContext.getProperty("db_role"), chat);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new ConflictException();
            }
            throw new InternalServerErrorException();
        }
        return uriInfo.getAbsolutePathBuilder()
                .path(chatCompany)
                .build();
    }

    public Chat getChatByCompany(String company) {
        try {
            Chat chat = chatDao.getByCompany((Role) requestContext.getProperty("db_role"), company, (String) requestContext.getProperty("username"));
            if (chat == null) {
                throw new NotFoundException();
            }
            return chat;
        } catch (SQLException e) {
            throw new InternalServerErrorException();
        }
    }

    public List<AdminChat> listAllChats() {
        try {
            return chatDao.list((Role) requestContext.getProperty("db_role"));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new InternalServerErrorException();
        }
    }

    public CompanyChat getCompanyChatWithAdmin(String companyUsername) {
        try {
            // For companies, they only have one chat - with the admin
            // Get their chat data and return it as a CompanyChat (participant = "Admin")
            Chat chat = chatDao.getByCompany((Role) requestContext.getProperty("db_role"), companyUsername, companyUsername);
            if (chat != null) {
                // Convert to CompanyChat format
                return new CompanyChat(companyUsername, chat.getUnread(), chat.getLastMsg());
            }
            return null;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new InternalServerErrorException();
        }
    }
}
