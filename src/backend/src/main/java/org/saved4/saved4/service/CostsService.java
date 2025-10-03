package org.saved4.saved4.service;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import org.saved4.saved4.dao.CostsDao;
import org.saved4.saved4.dto.Costs;
import org.saved4.saved4.enums.Role;

import java.sql.SQLException;

@RequestScoped
public class CostsService {
    @Inject
    CostsDao costsDao;

    @Context
    ContainerRequestContext requestContext;

    public Costs getCosts() {
        try {
            return costsDao.get((Role) requestContext.getProperty("db_role"));
        } catch (SQLException e) {
            throw new InternalServerErrorException();
        }
    }

    public void updateCosts(Costs costs) {
        try {
            costsDao.update((Role) requestContext.getProperty("db_role"), costs);
        } catch (SQLException e) {
            throw new InternalServerErrorException();
        }
    }
}
