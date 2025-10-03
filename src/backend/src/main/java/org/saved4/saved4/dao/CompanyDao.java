package org.saved4.saved4.dao;


import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.postgresql.geometric.PGpoint;
import org.saved4.saved4.DBProvider;
import org.saved4.saved4.dto.Company;
import org.saved4.saved4.dto.User;
import org.saved4.saved4.enums.Role;

@Singleton
public class CompanyDao {

    @Inject
    UserDao userDao;

    /**
     * Returns the first @param Limit number of rows from the company table in the database.
     *
     * @return a list of companies from the database
     */
    public List<Company> list(Role role) throws SQLException {
        // name, username, lat, long, accountType, openingTime, closingTime
        String query =
                "SELECT c.name, c.username, c.location, c.opening_time, c.closing_time, u.email FROM companies c, users u" +
                        " WHERE c.username = u.username AND u.is_active = TRUE";

        Connection connection = DBProvider.getConnection(role);
        PreparedStatement pb = connection.prepareStatement(query);
        ResultSet result = pb.executeQuery();

        List<Company> companyList = new ArrayList<>();
        while (result.next()) {
            companyList.add(mapToCompany(result));
        }
        return companyList;
    }

    /**
     * Return the company based on their username.
     *
     * @param company is the username given to the company
     * @return the selected company
     */
    public Company getByUsername(Role role, String dbRequester, String company)
            throws SQLException {
        System.out.println(company);
        String query =
                "SELECT c.name, c.username, c.location, c.opening_time, c.closing_time, u.email, u.created_at FROM companies c, users u WHERE c.username = ? AND c.username = u.username AND u.is_active = TRUE;";
        try (Connection connection = DBProvider.getConnection(role, dbRequester);
             PreparedStatement pb = connection.prepareStatement(query)) {

            pb.setString(1, company);

            try (ResultSet result = pb.executeQuery()) {
                if (result.next()) {
                    Company mappedCompany = mapToCompany(result);

                    Timestamp created_at = result.getTimestamp("created_at");
                    mappedCompany.setCreated_at(created_at);

                    return mappedCompany;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public String create(Role role, Company newCompany) throws SQLException {
        userDao.create(role, newCompany);
        String companyInsertStmt =
                "INSERT INTO companies (username, name, location, opening_time, closing_time) VALUES (?, ?, ?, ?, ?);";
        String chatInsertStmt = "INSERT INTO chats (company) VALUES (?)";

        Connection conn = DBProvider.getConnection(role);
        PreparedStatement companyPreparedStmt = conn.prepareStatement(companyInsertStmt);

        companyPreparedStmt.setString(1, newCompany.getUsername());
        companyPreparedStmt.setString(2, newCompany.getName());
        companyPreparedStmt.setObject(3, newCompany.getLocation());
        companyPreparedStmt.setObject(4, newCompany.getOpeningTime());
        companyPreparedStmt.setObject(5, newCompany.getClosingTime());

        PreparedStatement chatPreparedStmt = conn.prepareStatement(chatInsertStmt);

        chatPreparedStmt.setString(1, newCompany.getUsername());

        companyPreparedStmt.executeUpdate();
        chatPreparedStmt.executeUpdate();
        conn.commit();

        return newCompany.getUsername();
    }

    public boolean update(Role role, String dbRequester, String username, Company company)
            throws SQLException {
        userDao.update(role, dbRequester, username, company);
        System.out.println("here");

        StringBuilder updateQuery = new StringBuilder("UPDATE companies SET ");

        List<Object> params = new ArrayList<>();

        if (company.getUsername() != null) {
            updateQuery.append("username = ?, ");
            params.add(company.getUsername());
        }
        if (company.getName() != null) {
            updateQuery.append("name = ?, ");
            params.add(company.getName());
        }
        if (company.getLocation() != null) {
            updateQuery.append("location = ?, ");
            params.add(company.getLocation());
        }
        if (company.getOpeningTime() != null) {
            updateQuery.append("opening_time = ?, ");
            params.add(company.getOpeningTime());
        }
        if (company.getClosingTime() != null) {
            updateQuery.append("closing_time = ?, ");
            params.add(company.getClosingTime());
        }


        if (params.isEmpty()) {
            System.out.println("No fields to update for company.");
            return false;
        }

        updateQuery.setLength(updateQuery.length() - 2);

        updateQuery.append(
                " WHERE username = ? AND username IN (SELECT username FROM users WHERE username = ? AND is_active = TRUE)");
        params.add(username);
        params.add(username);

        Connection conn = null;
        PreparedStatement preparedStmt = null;
        try {
            conn = DBProvider.getConnection(role, dbRequester);
            conn.setAutoCommit(false); // Start transaction

            preparedStmt = conn.prepareStatement(updateQuery.toString());
            for (int i = 0; i < params.size(); i++) {
                preparedStmt.setObject(i + 1, params.get(i));
            }

            int affectedRows = preparedStmt.executeUpdate();
            conn.commit();
            return affectedRows > 0;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            System.err.println("SQL Exception during company update: " + e.getMessage());
            e.printStackTrace(); // Log the full stack trace for debugging
            ; // Re-throw the original exception to inform the caller
        } finally {
            // Close resources
            if (preparedStmt != null) {
                try {
                    preparedStmt.close();
                } catch (SQLException e) {
                    System.err.println("Error closing PreparedStatement: " + e.getMessage());
                }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit for connection pooling
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing Connection: " + e.getMessage());
                }
            }
        }
        return false;
    }


    public boolean delete(Role role, String dbRequester, String username) throws SQLException {
        String stmt =
                "UPDATE users u SET is_active = FALSE FROM companies c WHERE u.username = ? AND u.username = c.username AND u.is_active = TRUE";
        Connection conn = DBProvider.getConnection(role, dbRequester);
        PreparedStatement preparedStmt = conn.prepareStatement(stmt);
        preparedStmt.setString(1, username);

        int affectedRows = preparedStmt.executeUpdate();
        conn.commit();
        return affectedRows > 0;
    }

    public List<User> getAllCompaniesDB(Role role) throws SQLException {
        return new ArrayList<>(list(role));
    }

    public Company mapToCompany(ResultSet resultSet) throws SQLException {
        String username = resultSet.getString("username");
        String name = resultSet.getString("name");
        PGpoint location = resultSet.getObject("location", PGpoint.class);
        Time openingTime = resultSet.getTime("opening_time");
        Time closingTime = resultSet.getTime("closing_time");
        String email = resultSet.getString("email");

        return new Company(username, name, location, openingTime, closingTime, email);
    }
}
