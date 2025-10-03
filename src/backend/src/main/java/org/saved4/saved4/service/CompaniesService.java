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
import java.util.List;
import org.saved4.saved4.dao.CompanyDao;
import org.saved4.saved4.dto.Company;
import org.saved4.saved4.enums.Role;
import org.saved4.saved4.exceptions.ConflictException;

@RequestScoped
public class CompaniesService {
    @Inject
    private CompanyDao companyDao;
    @Context
    private ContainerRequestContext requestContext;
    @Context
    private UriInfo uriInfo;

    public List<Company> getAllCompanies() {
        System.out.println("[COMPANIES SERVICE] : Retrieving list of companies");

        try {
            return companyDao.list((Role) requestContext.getProperty("db_role"));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new InternalServerErrorException();
        }
    }

    public Company getCompanyByUsername(String username) {
        System.out.println("[COMPANIES SERVICE] : Retrieving company with ID -- " + username);

        try {
            Company company = companyDao.getByUsername((Role) requestContext.getProperty("db_role"),
                                                       (String) requestContext.getProperty(
                                                               "username"), username);
            if (company == null) {
                throw new NotFoundException();
            }

            return company;
        } catch (SQLException e) {
            throw new InternalServerErrorException();
        }
    }

    public URI createCompany(Company company) {
        String companyUsername;
        try {
            companyUsername =
                    companyDao.create((Role) requestContext.getProperty("db_role"), company);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new ConflictException();
            }
            throw new InternalServerErrorException();
        }
        return uriInfo.getAbsolutePathBuilder().path(companyUsername).build();
    }

    public void updateCompany(String username, Company company) {
        try {
            if (!companyDao.update((Role) requestContext.getProperty("db_role"),
                                   (String) requestContext.getProperty("username"), username,
                                   company)) {
                throw new NotFoundException();
            }
        } catch (SQLException e) {
            throw new InternalServerErrorException();
        }
    }

    public void deleteCompany(String username) {
        System.out.println("[COMPANIES SERVICE] : Deleting company " + username);

        try {
            if (!companyDao.delete((Role) requestContext.getProperty("db_role"),
                                   (String) requestContext.getProperty("username"), username)) {
                throw new NotFoundException();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new InternalServerErrorException();
        }
    }

}
