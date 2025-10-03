package org.saved4.saved4.dao;

import jakarta.inject.Singleton;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.postgresql.geometric.PGpoint;
import org.saved4.saved4.DBProvider;
import org.saved4.saved4.dto.Admin;
import org.saved4.saved4.dto.Company;
import org.saved4.saved4.dto.User;
import org.saved4.saved4.enums.Role;
import org.saved4.saved4.enums.UserType;

@Singleton
public class UserDao {

    public String create(Role role, User newUser) throws SQLException {
        String userInsertStmt =
                "INSERT INTO users (username, password, email, type) VALUES (?, ?, ?, ?::user_type);";
        Connection conn = DBProvider.getConnection(role);
        PreparedStatement preparedStmt = conn.prepareStatement(userInsertStmt);

        preparedStmt.setString(1, newUser.getUsername());
        preparedStmt.setString(2, newUser.getPassword());
        preparedStmt.setString(3, newUser.getEmail());
        preparedStmt.setString(4, newUser.getUserType().toString().toLowerCase());

        preparedStmt.executeUpdate();
        conn.commit();

        return newUser.getUsername();
    }

    public boolean update(Role role, String dbRequester, String username, User user)
            throws SQLException {
        StringBuilder updateQuery = new StringBuilder("UPDATE users SET ");

        List<Object> params = new ArrayList<>();

        if (user.getUsername() != null) {
            updateQuery.append("username = ?, ");
            params.add(user.getUsername());
        }
        if (user.getPassword() != null) {
            updateQuery.append("password = ?, ");
            params.add(user.getPassword());
        }
        if (user.getEmail() != null) {
            updateQuery.append("email = ?, ");
            params.add(user.getEmail());
        }
        if (user.getUserType() != null) {
            updateQuery.append("type = ?::user_type, ");
            params.add(user.getUserType().toString().toLowerCase());
        }

        if (user.getModified_at() != null) {
            updateQuery.append("modified_at = ?, ");
            params.add(user.getModified_at());
        }

        if (params.isEmpty()) {
            return false;
        }

        updateQuery.setLength(updateQuery.length() - 2);
        updateQuery.append(" WHERE username = ? AND is_active = TRUE; ");
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

    public User getUserByUsername(Role role, String username) throws SQLException {
        String userQuery = "SELECT u.username, u.email, u.created_at, u.modified_at, u.type, " +
                "COALESCE(c.name, a.name, u.username) as name, " +
                "COALESCE(c.location, p.location) as location, " +
                "c.opening_time, c.closing_time " + "FROM users u " +
                "LEFT JOIN companies c ON u.username = c.username " +
                "LEFT JOIN admins a ON u.username = a.username " +
                "LEFT JOIN port p ON u.username = p.username " +
                "WHERE u.is_active = TRUE AND u.username = ? AND u.type != 'port'::user_type";
        Connection conn = DBProvider.getConnection(role);
        PreparedStatement preparedStmt = conn.prepareStatement(userQuery);

        preparedStmt.setString(1, username);
        ResultSet resultSet = preparedStmt.executeQuery();
        if (!resultSet.next()) {
            return null;
        }

        return mapResultSetToUser(resultSet);
    }

    public List<User> getAllUsers(Role role, String type) throws SQLException {
        StringBuilder queryBuilder = new StringBuilder(
                "SELECT u.username, u.email, u.created_at, u.modified_at, u.type, " +
                        "COALESCE(c.name, a.name, u.username) as name, " +
                        "COALESCE(c.location, p.location) as location, " +
                        "c.opening_time, c.closing_time " + "FROM users u " +
                        "LEFT JOIN companies c ON u.username = c.username " +
                        "LEFT JOIN admins a ON u.username = a.username " +
                        "LEFT JOIN port p ON u.username = p.username " +
                        "WHERE u.is_active = TRUE AND u.type != 'port'::user_type");

        List<Object> params = new ArrayList<>();

        if (type != null && !type.isEmpty() && !type.equalsIgnoreCase("All") &&
                !type.equalsIgnoreCase("null")) {
            queryBuilder.append(" AND u.type = ?::user_type");
            params.add(type.toLowerCase());
        }

        queryBuilder.append(" ORDER BY u.created_at DESC");

        Connection conn = DBProvider.getConnection(role);
        PreparedStatement preparedStmt = conn.prepareStatement(queryBuilder.toString());

        for (int i = 0; i < params.size(); i++) {
            preparedStmt.setObject(i + 1, params.get(i));
        }

        ResultSet resultSet = preparedStmt.executeQuery();
        List<User> users = new ArrayList<>();

        while (resultSet.next()) {
            users.add(mapResultSetToUser(resultSet));
        }

        return users;
    }

    /**
     * Helper method to map a ResultSet row to a User, Company, or Admin object.
     *
     * @param resultSet The ResultSet containing user data.
     * @return A User, Company, or Admin object populated with data from the ResultSet.
     * @throws SQLException If a database access error occurs.
     */
    private User mapResultSetToUser(ResultSet resultSet) throws SQLException {
        UserType userType = UserType.valueOf(resultSet.getString("type").toUpperCase());
        System.out.println(
                "Processing user: " + resultSet.getString("username") + " of type: " + userType);
        System.out.println("Name from query: " + resultSet.getString("name"));

        User user;

        if (userType == UserType.COMPANY) {
            Company company = new Company();
            // Handle location
            Object locationObj = resultSet.getObject("location");
            if (locationObj instanceof PGpoint) {
                company.setLocation((PGpoint) locationObj);
            }
            // Handle times
            company.setOpeningTime(resultSet.getTime("opening_time"));
            company.setClosingTime(resultSet.getTime("closing_time"));
            company.setName(resultSet.getString("name"));
            user = company;
        } else if (userType == UserType.ADMIN) {
            Admin admin = new Admin();
            admin.setName(resultSet.getString("name"));
            user = admin;

        } else {
            user = new User();
        }

        user.setUsername(resultSet.getString("username"));
        user.setEmail(resultSet.getString("email"));
        user.setCreated_at(resultSet.getTimestamp("created_at"));
        user.setModified_at(resultSet.getTimestamp("modified_at"));
        user.setUserType(userType);

        return user;
    }
}
