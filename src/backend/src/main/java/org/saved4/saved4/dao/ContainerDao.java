package org.saved4.saved4.dao;

import jakarta.inject.Singleton;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.saved4.saved4.DBProvider;
import org.saved4.saved4.dto.Container;
import org.saved4.saved4.enums.Role;

@Singleton
public class ContainerDao {
    public Container getById(Role role, int id) throws SQLException {
        String query = "SELECT * FROM containers WHERE container_id = ? AND is_active = TRUE";

        Connection conn = DBProvider.getConnection(role);
        PreparedStatement preparedStmt = conn.prepareStatement(query);
        preparedStmt.setInt(1, id);

        ResultSet resultSet = preparedStmt.executeQuery();
        if (!resultSet.next()) {
            return null;
        }
        int containerId = resultSet.getInt("container_id");
        float maxWeight = resultSet.getFloat("max_weight");
        return new Container(containerId, maxWeight);
    }

    public List<Container> list(Role role) throws SQLException {

        String query = "SELECT * FROM containers WHERE is_active = TRUE;";

        Connection conn = DBProvider.getConnection(role);
        Statement stmt = conn.createStatement();

        List<Container> containers = new ArrayList<>();
        ResultSet resultSet = stmt.executeQuery(query);
        while (resultSet.next()) {
            int containerId = resultSet.getInt("container_id");
            float maxWeight = resultSet.getFloat("max_weight");

            containers.add(new Container(containerId, maxWeight));
        }
        return containers;
    }

    public List<Container> list(Role role, String dbRequester, String atCompany, Timestamp twStart,
                                Timestamp twEnd) throws SQLException {
        String query = "SELECT container_id FROM (SELECT DISTINCT ON (container_id)" +
                " container_id, destination, tw_end FROM orders ORDER BY container_id, tw_end DESC) last_orders," +
                " containers c WHERE c.is_active = TRUE AND destination = ? AND tw_end < ? AND NOT EXISTS (SELECT 1" +
                " FROM orders o WHERE o.container_id = last_orders.container_id AND o.tw_start < ? AND o.tw_end > ?);";

        Connection conn = DBProvider.getConnection(role, dbRequester);
        PreparedStatement preparedStmt = conn.prepareStatement(query);
        preparedStmt.setString(1, atCompany);
        preparedStmt.setTimestamp(2, twStart);
        preparedStmt.setTimestamp(3, twEnd);
        preparedStmt.setTimestamp(4, twStart);

        List<Container> containers = new ArrayList<>();
        ResultSet resultSet = preparedStmt.executeQuery();
        while (!resultSet.next()) {
            int containerId = resultSet.getInt("container_id");
            float maxWeight = resultSet.getFloat("max_weight");

            containers.add(new Container(containerId, maxWeight));
        }
        return containers;
    }

    public Integer create(Role role, Container container) throws SQLException {
        Connection conn = DBProvider.getConnection(role);
        PreparedStatement preparedStmt;
        String stmt;
        if (container.getContainerId() == null) {
            stmt = "INSERT INTO containers (max_weight) VALUES (?);";
            preparedStmt = conn.prepareStatement(stmt, Statement.RETURN_GENERATED_KEYS);
            preparedStmt.setInt(1, container.getContainerId());
        } else {
            stmt = "INSERT INTO containers (container_id, max_weight) VALUES (?, ?);";
            preparedStmt = conn.prepareStatement(stmt, Statement.RETURN_GENERATED_KEYS);
        }
        preparedStmt.setFloat(1, container.getMaxWeight());

        preparedStmt.executeUpdate();
        conn.commit();
        ResultSet generatedKeys = preparedStmt.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getInt(1);
        } else {
            throw new SQLException("Creating user failed, no ID obtained.");
        }
    }


    public boolean update(Role role, int containerId, Container container) throws SQLException {
        StringBuilder updateQuery = new StringBuilder("UPDATE containers SET ");
        List<Object> params = new ArrayList<>();
        List<Integer> paramTypes = new ArrayList<>();


        if (container.getContainerId() != null) {
            updateQuery.append("container_id = ?, ");
            params.add(container.getContainerId());
            paramTypes.add(Types.INTEGER);
        }
        if (container.getMaxWeight() != null) {
            updateQuery.append("max_weight = ?, ");
            params.add(container.getMaxWeight());
            paramTypes.add(Types.DECIMAL);
        }
        if (container.getContainerStatus() != null) {
            updateQuery.append("container_status = ?::container_status, ");
            params.add(container.getContainerStatus().name());
            paramTypes.add(Types.OTHER);
        }

        if (params.isEmpty()) {
            System.out.println("No fields provided for update for containerId: " + containerId);
            return false;
        }

        updateQuery.setLength(updateQuery.length() - 2);
        updateQuery.append(" WHERE container_id = ? AND is_active = TRUE; ");
        params.add(containerId);
        paramTypes.add(Types.INTEGER);

        Connection conn = null;
        PreparedStatement preparedStmt = null;
        boolean updateSuccessful = false;

        try {
            conn = DBProvider.getConnection(role);
            conn.setAutoCommit(false);

            preparedStmt = conn.prepareStatement(updateQuery.toString());

            for (int i = 0; i < params.size(); i++) {
                preparedStmt.setObject(i + 1, params.get(i), paramTypes.get(i));
            }

            int affectedRows = preparedStmt.executeUpdate();
            conn.commit();
            updateSuccessful = affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("SQL error during container update for containerId: " + containerId);
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Transaction rolled back for containerId: " + containerId);
                } catch (SQLException rollbackEx) {
                    System.err.println(
                            "Error during transaction rollback for containerId: " + containerId);
                    rollbackEx.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (preparedStmt != null) {
                try {
                    preparedStmt.close();
                } catch (SQLException e) {
                    System.err.println("Error closing PreparedStatement");
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing Connection");
                    e.printStackTrace();
                }
            }
        }
        return updateSuccessful;
    }

    public boolean delete(Role role, int containerId) throws SQLException {
        String stmt =
                "UPDATE containers SET isActive = FALSE WHERE container_id = ? AND is_active = TRUE;";

        Connection conn = DBProvider.getConnection(role);
        PreparedStatement preparedStmt = conn.prepareStatement(stmt);
        preparedStmt.setInt(1, containerId);

        int affectedRows = preparedStmt.executeUpdate();
        conn.commit();
        return affectedRows > 0;
    }
}
