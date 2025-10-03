package org.saved4.saved4.dao;

import jakarta.inject.Singleton;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.saved4.saved4.DBProvider;
import org.saved4.saved4.auth.PasswordHasher;
import org.saved4.saved4.enums.Role;
import org.saved4.saved4.enums.UserType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Singleton
public class AuthDao {

    public Pair<String, UserType> login(Role role, String username, String plaintextPassword) throws SQLException {

        Connection conn = DBProvider.getConnection(role);
        String stmt = "select password from users where username = ?";
        PreparedStatement preparedStmt = conn.prepareStatement(stmt);

        preparedStmt.setString(1, username);

        ResultSet resultSet = preparedStmt.executeQuery();
        if (!resultSet.next()) {
            return null;
        }

        if (!PasswordHasher.verifyPassword(resultSet.getString("password"), plaintextPassword)) return null;

        System.out.println("password verified");
        stmt = "SELECT type FROM users WHERE username = ?";
        preparedStmt = conn.prepareStatement(stmt);

        preparedStmt.setString(1, username);

        resultSet = preparedStmt.executeQuery();
        if (!resultSet.next()) {
            return null;
        }
        System.out.println("got type");

        UserType userType = UserType.valueOf(((String) resultSet.getObject("type")).toUpperCase());
        System.out.println(userType);
        if (userType == UserType.PORT) {
            return null;
        }
        if (userType == UserType.ADMIN) {
            stmt = "SELECT name FROM admins WHERE username = ?";
        } else {
            stmt = "SELECT name FROM companies WHERE username = ?";
        }
        preparedStmt = conn.prepareStatement(stmt);

        preparedStmt.setString(1, username);

        resultSet = preparedStmt.executeQuery();
        if (!resultSet.next()) {
            return null;
        }
        String name = resultSet.getString("name");

        return new ImmutablePair<>(name, userType);
    }

    public boolean updatePassword(Role role, String username, String hashedPassword) throws SQLException {
        Connection conn = DBProvider.getConnection(role);
        String stmt = "UPDATE users SET password = ?, modified_at = CURRENT_TIMESTAMP WHERE username = ?";
        PreparedStatement preparedStmt = conn.prepareStatement(stmt);

        preparedStmt.setString(1, hashedPassword);
        preparedStmt.setString(2, username);

        int affectedRows = preparedStmt.executeUpdate();
        conn.commit();
        return affectedRows > 0;
    }
}
