package org.saved4.saved4.service;


import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import java.sql.SQLException;
import java.util.List;
import org.saved4.saved4.dao.CompanyDao;
import org.saved4.saved4.dao.UserDao;
import org.saved4.saved4.dto.User;
import org.saved4.saved4.enums.Role;

@RequestScoped
public class UserService {

    @Inject
    private UserDao userDao;
    @Inject
    private CompanyDao companyDao;
    @Inject
    private CompaniesService companiesService;
    @Context
    private ContainerRequestContext requestContext;

    public List<User> getAllCompanies() {
        try {
            return companyDao.getAllCompaniesDB((Role) requestContext.getProperty("db_role"));
        } catch (SQLException e) {
            throw new InternalServerErrorException();
        }
    }

    public User getUserByUsername(String username) {
        System.out.println("[USER SERVICE]: Retrieving Company: " + username);
        try {
            return userDao.getUserByUsername((Role) requestContext.getProperty("db_role"),
                                             username);
        } catch (SQLException e) {
            throw new InternalServerErrorException();
        }
    }

    public List<User> getAllUsers(String type) {
        try {
            return userDao.getAllUsers((Role) requestContext.getProperty("db_role"), type);
        } catch (SQLException e) {
            throw new InternalServerErrorException("Failed to retrieve users: " + e.getMessage());
        }
    }
}
