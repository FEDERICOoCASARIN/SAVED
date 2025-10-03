package org.saved4.saved4.dao;

import jakarta.inject.Singleton;
import org.saved4.saved4.DBProvider;
import org.saved4.saved4.dto.Costs;
import org.saved4.saved4.enums.Role;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class CostsDao {
    public Costs get(Role role) throws SQLException {
        String query = "SELECT * FROM costs;";
        Connection conn = DBProvider.getConnection(role);
        ResultSet resultSet = conn.createStatement().executeQuery(query);
        if (!resultSet.next()) {
            return null;
        }
        float empty = resultSet.getFloat("empty_ctr");
        float full = resultSet.getFloat("full_ctr");
        float perKm = resultSet.getFloat("per_km");

        return new Costs(empty, full, perKm);
    }

    public boolean update(Role role, Costs costs) throws SQLException {
        StringBuilder stmt = new StringBuilder("UPDATE FROM costs SET ");
        List<Object> params = new ArrayList<>();

        if (costs.getEmptyCtr() != null) {
            stmt.append("empty_ctr = ?, ");
            params.add(costs.getEmptyCtr());
        }
        if (costs.getFullCtr() != null) {
            stmt.append("full_ctr = ?, ");
            params.add(costs.getFullCtr());
        }
        if (costs.getPerKm() != null) {
            stmt.append("per_km = ?, ");
            params.add(costs.getPerKm());
        }

        if (params.isEmpty()) {
            return false;
        }
        stmt.setLength(stmt.length() - 2);

        Connection conn = DBProvider.getConnection(role);
        int affectedRows = conn.createStatement().executeUpdate(stmt.toString());
        conn.commit();
        return affectedRows > 0;
    }
}
