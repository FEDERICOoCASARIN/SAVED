package org.saved4.saved4.dao;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.saved4.saved4.DBProvider;
import org.saved4.saved4.dto.Admin;
import org.saved4.saved4.dto.Company;
import org.saved4.saved4.enums.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Singleton
public class RegisterDao {
    @Inject
    CompanyDao companyDao;
    @Inject
    AdminDao adminDao;

    public String registerCompany(Role role, Company company) throws SQLException {
        return companyDao.create(role, company);
    }

    public String registerAdmin(Role role, Admin admin) throws SQLException {
        return adminDao.create(role, admin);
    }

    public boolean checkIfExists(Role role, String email) throws SQLException {
        String stmt = "SELECT 1 FROM users where email = ?";
        Connection conn = DBProvider.getConnection(role);
        PreparedStatement preparedStmt = conn.prepareStatement(stmt);
        preparedStmt.setString(1, email);
        ResultSet resultSet = preparedStmt.executeQuery();

        return resultSet.next();
    }
}
