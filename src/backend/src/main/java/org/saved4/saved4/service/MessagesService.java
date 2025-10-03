package org.saved4.saved4.service;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import org.saved4.saved4.dao.MessageDao;
import org.saved4.saved4.dto.Message;
import org.saved4.saved4.enums.Role;
import org.saved4.saved4.exceptions.ConflictException;

import java.net.URI;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@RequestScoped
public class MessagesService {
    @Inject
    MessageDao messageDao;

    @Context
    ContainerRequestContext requestContext;
    @Context
    UriInfo uriInfo;

    public URI createMessage(String company, String body) {
        Integer msgId;
        try {
            msgId = messageDao.create(new Message(company, new Timestamp(System.currentTimeMillis()),
                    (String) requestContext.getProperty("username"), body), (Role) requestContext.getProperty("db_role"));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            if ("23505".equals(e.getSQLState())) {
                throw new ConflictException();
            }
            throw new InternalServerErrorException();
        }
        if (msgId == null) {
            return null;
        }
        return uriInfo.getAbsolutePathBuilder()
                .path(Long.toString(msgId))
                .build();
    }

    public List<Message> listMessages(String company) {
        try {
            return messageDao.list(company, (String) requestContext.getProperty("username"), (Role) requestContext.getProperty("db_role"));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new InternalServerErrorException();
        }
    }
}
