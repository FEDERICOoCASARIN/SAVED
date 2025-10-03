package org.saved4.saved4.service;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import org.saved4.saved4.dao.ContainerDao;
import org.saved4.saved4.dto.Container;
import org.saved4.saved4.enums.Role;
import org.saved4.saved4.exceptions.ConflictException;

@RequestScoped
public class ContainerService {
    @Inject
    private ContainerDao containerDao;
    @Context
    ContainerRequestContext requestContext;
    @Context
    UriInfo uriInfo;

    public Container getContainerById(int containerId) {
        try {
            Container container =
                    containerDao.getById((Role) requestContext.getProperty("db_role"), containerId);
            if (container == null) {
                throw new NotFoundException();
            }
            return container;
        } catch (SQLException e) {
            throw new InternalServerErrorException();
        }
    }

    public List<Container> getAllContainers() {
        try {
            List<Container> container =
                    containerDao.list((Role) requestContext.getProperty("db_role"));
            if (container == null) {
                throw new NotFoundException();
            }
            return container;
        } catch (SQLException e) {
            throw new InternalServerErrorException();
        }
    }

    public List<Container> listContainers(String atCompany, Timestamp twStart, Timestamp twEnd) {
        try {
            return containerDao.list((Role) requestContext.getProperty("db_role"),
                                     (String) requestContext.getProperty("username"), atCompany,
                                     twStart, twEnd);
        } catch (SQLException e) {
            throw new InternalServerErrorException();
        }
    }

    public URI createContainer(Integer containerId, float maxWeight) {
        Integer newContainerId;
        try {
            newContainerId = containerDao.create((Role) requestContext.getProperty("db_role"),
                                                 containerId == null ? new Container(maxWeight) :
                                                         new Container(containerId, maxWeight));
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new ConflictException();
            }
            throw new InternalServerErrorException();
        }
        return uriInfo.getAbsolutePathBuilder().path(Long.toString(newContainerId)).build();
    }

    public void updateContainer(int containerId, Container container) {
        try {
            if (!containerDao.update((Role) requestContext.getProperty("db_role"), containerId,
                                     container)) {
                throw new NotFoundException();
            }
        } catch (SQLException e) {
            throw new InternalServerErrorException();
        }
    }

    public void deleteContainer(int containerId) {
        try {
            if (!containerDao.delete((Role) requestContext.getProperty("db_role"), containerId)) {
                throw new NotFoundException();
            }
        } catch (SQLException e) {
            throw new InternalServerErrorException();
        }
    }
}
