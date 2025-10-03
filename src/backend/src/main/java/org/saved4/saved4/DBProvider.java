package org.saved4.saved4;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.saved4.saved4.enums.Role;

public final class DBProvider {
    private static final String HOST = "bronto.ewi.utwente.nl";
    private static final String DB_NAME = "dab_di2425-2b_14";
    private static final String URL =
            "jdbc:postgresql://" + HOST + ":5432/" + DB_NAME + "?currentSchema=saved4";
    private static final String USERNAME = "dab_di2425-2b_14";


    private DBProvider() {
    }

    public static Connection getConnection(Role role) throws SQLException {
        String password = System.getenv("SAVED4_DB_PASSWORD");

        Connection con = DriverManager.getConnection(URL, USERNAME, password);
        con.setAutoCommit(false);
        String setRoleStmt = "SELECT set_config('saved4.role', ?, true) ;";

        PreparedStatement stmt = con.prepareStatement(setRoleStmt);
        stmt.setString(1, role.toString());
        stmt.executeQuery();

        return con;
    }

    public static Connection getConnection(Role role, String username) throws SQLException {
        String password = System.getenv("SAVED4_DB_PASSWORD");

        Connection con = DriverManager.getConnection(URL, USERNAME, password);
        con.setAutoCommit(false);
        String setRoleStmt = "SELECT set_config('saved4.role', ?, true) ;";
        String setUsernameStmt = "SELECT set_config('saved4.username', ?, true) ;";

        PreparedStatement roleStmt = con.prepareStatement(setRoleStmt);
        roleStmt.setString(1, role.toString());
        roleStmt.executeQuery();

        PreparedStatement usernameStmt = con.prepareStatement(setUsernameStmt);
        usernameStmt.setString(1, username);
        usernameStmt.executeQuery();

        return con;
    }
}
