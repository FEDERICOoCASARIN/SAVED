package org.saved4.saved4;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.saved4.saved4.auth.PasswordHasher;
import org.saved4.saved4.enums.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@ApplicationPath("/api")
public class SavedApplication extends Application {
    public SavedApplication() {
        try {
            Class.forName("org.postgresql.Driver");
            setup();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void setup() {
        try {
            Connection conn = DBProvider.getConnection(Role.SYSTEM);
            String stmt = "INSERT INTO USERS VALUES ('admin', ?, 'admin', CURRENT_TIMESTAMP, null, 'admin')";
            PreparedStatement preparedStmt = conn.prepareStatement(stmt);
            preparedStmt.setString(1, PasswordHasher.hashPassword("admin"));
            preparedStmt.executeUpdate();

            stmt = "INSERT INTO admins VALUES ('admin', 'admin')";
            conn.createStatement().executeUpdate(stmt);
            conn.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
