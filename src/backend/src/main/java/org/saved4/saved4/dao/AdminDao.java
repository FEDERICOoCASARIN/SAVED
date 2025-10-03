package org.saved4.saved4.dao;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.saved4.saved4.DBProvider;
import org.saved4.saved4.dto.Admin;
import org.saved4.saved4.enums.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class AdminDao {

    @Inject
    UserDao userDao;

    public String create(Role role, Admin newAdmin) throws SQLException {
        userDao.create(role, newAdmin);
        String adminInsertStmt = "INSERT INTO admins VALUES (?, ?)";

        Connection conn = DBProvider.getConnection(role);
        PreparedStatement companyPreparedStmt = conn.prepareStatement(adminInsertStmt);

        companyPreparedStmt.setString(1, newAdmin.getUsername());
        companyPreparedStmt.setString(2, newAdmin.getName());

        companyPreparedStmt.executeUpdate();
        conn.commit();

        return newAdmin.getUsername();
    }

    public boolean update(Role role, String dbRequester, String username, Admin admin) throws SQLException {
        userDao.update(role, dbRequester, username, admin);

        StringBuilder updateQuery =
                new StringBuilder("UPDATE admins a SET ");

        List<Object> params = new ArrayList<>();

        if (admin.getUsername() != null) {
            updateQuery.append("a.username = ?, ");
            params.add(admin.getUsername());
        }
        if (admin.getName() != null) {
            updateQuery.append("a.name = ?, ");
            params.add(admin.getName());
        }

        if (params.isEmpty()) {
            return false;
        }

        updateQuery.setLength(updateQuery.length() - 2);
        updateQuery.append("FROM users u WHERE a.username = ? AND a.username = u.username AND u.is_active = TRUE; ");
        params.add(username);

        Connection conn = DBProvider.getConnection(role, dbRequester);
        PreparedStatement preparedStmt = conn.prepareStatement(updateQuery.toString());
        for (int i = 0; i < params.size(); i++) {
            preparedStmt.setObject(i + 1, params.get(i));
        }
        int affectedRows = preparedStmt.executeUpdate();
        conn.commit();
        return affectedRows > 0;
    }

    public boolean delete(Role role, String dbRequester, String username) throws SQLException {
        String stmt = "UPDATE users u SET u.is_active = FALSE FROM companies c WHERE u.username = ? AND u.username = c.username AND u.is_active = TRUE";
        Connection conn = DBProvider.getConnection(role, dbRequester);
        PreparedStatement preparedStmt = conn.prepareStatement(stmt);
        preparedStmt.setString(1, username);

        int affectedRows = preparedStmt.executeUpdate();
        conn.commit();
        return affectedRows > 0;
    }
}
