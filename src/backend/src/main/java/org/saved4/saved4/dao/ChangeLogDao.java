package org.saved4.saved4.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Singleton;
import org.postgresql.util.PGobject;
import org.saved4.saved4.DBProvider;
import org.saved4.saved4.dto.ChangeLog;
import org.saved4.saved4.dto.ChangeStatus;
import org.saved4.saved4.enums.Role;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Data Access Object (DAO) for managing ChangeLog entries in the database.
 * Handles CRUD operations for order history logs.
 */
@Singleton
public class ChangeLogDao {

    private final ObjectMapper mapper = new ObjectMapper();
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Retrieves all change logs from the order_history table, ordered by change_id in descending order.
     * This method is typically used for an administrative view where all data is visible.
     *
     * @return A list of {@link ChangeLog} objects representing all change log entries.
     */
    @ApplicationScoped
    public List<ChangeLog> getAllChangeLogs() {
        List<ChangeLog> logs = new ArrayList<>();
        // IMPORTANT: Select the new 'isRead' column
        String query = "SELECT * FROM order_history ORDER BY change_id DESC";

        System.out.println("DAO: Attempting to retrieve all changelogs.");

        try (Connection connection = DBProvider.getConnection(Role.SYSTEM);
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                logs.add(mapResultSetToChangeLog(rs));
            }
            System.out.println("DAO: Successfully retrieved " + logs.size() + " changelog entries.");
        } catch (SQLException e) {
            System.err.println("DAO Error retrieving all changelogs: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("DAO Unexpected Error retrieving all changelogs: " + e.getMessage());
            e.printStackTrace();
        }
        return logs;
    }

    /**
     * Inserts a new change log entry into the order_history table.
     *
     * @param log The {@link ChangeLog} object containing the data to be inserted.
     */
    public void insertChangeLog(ChangeLog log) {
        // IMPORTANT: Add 'isRead' to the INSERT statement
        String query = "INSERT INTO order_history (order_id, modified_at, change_status, modified_by, order_owner, type_changed, data_initial, data_changed, isRead) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DBProvider.getConnection(Role.SYSTEM);
             PreparedStatement stmt = connection.prepareStatement(query)) {

            connection.setAutoCommit(true);

            stmt.setInt(1, log.getOrderId());
            stmt.setTimestamp(2, log.getModifiedAt());
            String statusForDb = log.getChangeStatus().stream()
                    .map(ChangeStatus::getDbValue)
                    .collect(Collectors.joining(","));
            stmt.setString(3, statusForDb);
            stmt.setString(4, log.getModifiedBy());
            stmt.setString(5, log.getOrderOwner());

            if (log.getTypeChanged() != null) {
                stmt.setArray(6, connection.createArrayOf("text", log.getTypeChanged().toArray()));
            } else {
                stmt.setNull(6, Types.ARRAY);
            }

            PGobject jsonInitial = new PGobject();
            jsonInitial.setType("jsonb");
            jsonInitial.setValue(mapper.writeValueAsString(log.getDataInitial() != null ? log.getDataInitial() : new HashMap<>()));
            stmt.setObject(7, jsonInitial);

            PGobject jsonChanged = new PGobject();
            jsonChanged.setType("jsonb");
            jsonChanged.setValue(mapper.writeValueAsString(log.getDataChanged() != null ? log.getDataChanged() : new HashMap<>()));
            stmt.setObject(8, jsonChanged);

            // NEW: Prepare 'isRead' for insertion
            PGobject jsonIsRead = new PGobject();
            jsonIsRead.setType("jsonb");
            // New changelogs start with an empty isRead map
            jsonIsRead.setValue(mapper.writeValueAsString(log.getIsRead() != null ? log.getIsRead() : new HashMap<>()));
            stmt.setObject(9, jsonIsRead);

            stmt.executeUpdate();
            System.out.println("DAO: Successfully inserted changelog for Order ID: " + log.getOrderId());
        } catch (Exception e) {
            System.err.println("DAO Error inserting changelog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Retrieves change logs filtered strictly by a specific order owner.
     * This method ensures a user only sees changes related to orders they own.
     *
     * @param orderOwner The ID of the order owner (e.g., company name or user ID) to filter by.
     * @return A list of {@link ChangeLog} entries associated with the given orderOwner.
     */
    public List<ChangeLog> getChangeLogsByOrderOwner(String orderOwner) {
        List<ChangeLog> logs = new ArrayList<>();
        String query = "SELECT *, isRead FROM order_history WHERE order_owner = ? ORDER BY change_id DESC";

        System.out.println("DAO: Attempting to retrieve changelogs for order owner: " + orderOwner + "...");

        try (Connection connection = DBProvider.getConnection(Role.SYSTEM);
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, orderOwner);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToChangeLog(rs));
                }
                System.out.println("DAO: Successfully retrieved " + logs.size() + " changelog entries for order owner: " + orderOwner + ".");
            }
        } catch (SQLException e) {
            System.err.println("DAO SQL Error retrieving changelogs for order owner " + orderOwner + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("DAO Unexpected Error retrieving changelogs for order owner " + orderOwner + ": " + e.getMessage());
            e.printStackTrace();
        }
        return logs;
    }

    /**
     * Helper method to map a ResultSet row to a ChangeLog object.
     *
     * @param rs The ResultSet containing the current row's data.
     * @return A {@link ChangeLog} object populated with data from the ResultSet.
     * @throws SQLException If a database access error occurs.
     */
    private ChangeLog mapResultSetToChangeLog(ResultSet rs) throws SQLException {
        ChangeLog log = new ChangeLog();
        log.setChangeId(rs.getInt("change_id"));
        log.setOrderId(rs.getInt("order_id"));
        log.setModifiedAt(rs.getTimestamp("modified_at"));

        String changeStatusString = rs.getString("change_status");
        if (changeStatusString != null && !changeStatusString.isEmpty()) {
            try {
                List<ChangeStatus> statuses = Arrays.stream(changeStatusString.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(s -> {
                            try {
                                return ChangeStatus.fromDbValue(s);
                            } catch (IllegalArgumentException e) {
                                System.err.println("Warning: Invalid ChangeStatus value '" + s + "' found for ChangeLog ID " + log.getChangeId() + ". Skipping. Error: " + e.getMessage());
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                log.setChangeStatus(statuses);
            } catch (Exception e) {
                System.err.println("Error parsing change_status string '" + changeStatusString + "' for ChangeLog ID " + log.getChangeId() + ". Error: " + e.getMessage());
                log.setChangeStatus(List.of());
            }
        } else {
            log.setChangeStatus(List.of());
        }

        log.setModifiedBy(rs.getString("modified_by"));
        log.setOrderOwner(rs.getString("order_owner"));

        Array array = rs.getArray("type_changed");
        if (array != null) {
            log.setTypeChanged(Arrays.asList((String[]) array.getArray()));
        } else {
            log.setTypeChanged(List.of());
        }

        String jsonInitial = rs.getString("data_initial");
        String jsonChanged = rs.getString("data_changed");
        // NEW: Get 'isRead' JSONB string
        String jsonIsRead = rs.getString("isRead");

        try {
            if (jsonInitial != null && !jsonInitial.isEmpty()) {
                log.setDataInitial(mapper.readValue(jsonInitial, Map.class));
            } else {
                log.setDataInitial(new HashMap<>());
            }

            if (jsonChanged != null && !jsonChanged.isEmpty()) {
                log.setDataChanged(mapper.readValue(jsonChanged, Map.class));
            } else {
                log.setDataChanged(new HashMap<>());
            }

            // NEW: Parse 'isRead' JSONB data
            if (jsonIsRead != null && !jsonIsRead.isEmpty()) {
                // We're expecting a Map<String, String> (username -> timestamp string)
                log.setIsRead(mapper.readValue(jsonIsRead, new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {
                }));
            } else {
                log.setIsRead(new HashMap<>()); // Default to empty map if null or empty
            }

        } catch (Exception e) {
            System.err.println("Error parsing JSONB data for ChangeLog ID " + log.getChangeId() + ". Initial JSON: '" + jsonInitial + "', Changed JSON: '" + jsonChanged + "', isRead JSON: '" + jsonIsRead + "'. Error: " + e.getMessage());
            log.setDataInitial(new HashMap<>());
            log.setDataChanged(new HashMap<>());
            log.setIsRead(new HashMap<>()); // Ensure it's initialized even on error
        }
        return log;
    }

    /**
     * Marks specified change logs as read by a given user.
     * This updates the 'isRead' JSONB column by adding/updating the user's read timestamp.
     *
     * @param changeIds A list of change log IDs to mark as read.
     * @param username  The username of the user who is marking the logs as read.
     */
    public void markLogsAsRead(List<Integer> changeIds, String username) {
        if (changeIds == null || changeIds.isEmpty() || username == null || username.trim().isEmpty()) {
            System.out.println("DAO: No change IDs or username provided to mark as read. Skipping.");
            return;
        }

        // Generate placeholders for the IN clause (?, ?, ?)
        String inSql = String.join(",", Collections.nCopies(changeIds.size(), "?"));

        // SQL to update the JSONB column:
        // We use 'jsonb_set' for precise updates.
        // '{}' is the initial value if the key doesn't exist (though our default is '{}').
        // '{username}' is the path for the key.
        // to_jsonb(CURRENT_TIMESTAMP::text) converts the current timestamp to a JSONB string value.
        // 'true' means create_if_missing.
        String query = String.format(
                "UPDATE order_history SET isRead = jsonb_set(isRead, ARRAY[?], to_jsonb(?::text), true) " +
                        "WHERE change_id IN (%s)", inSql
        );

        System.out.println("DAO: Attempting to mark changelogs as read for user: " + username + " IDs: " + changeIds);
        System.out.println("DAO: SQL Query for markLogsAsRead: " + query); // Debugging: Print the full query

        try (Connection connection = DBProvider.getConnection(Role.SYSTEM);
             PreparedStatement stmt = connection.prepareStatement(query)) {

            // Explicitly set auto-commit to true for this connection if it's not the default in DBProvider
            connection.setAutoCommit(true);

            // Set the username and current timestamp for the jsonb_set function
            int paramIndex = 1;
            String currentTimestampStr = LocalDateTime.now().format(ISO_FORMATTER);
            stmt.setString(paramIndex++, username); // First '?' in ARRAY[?]
            stmt.setString(paramIndex++, currentTimestampStr); // Second '?' for to_jsonb(?::text)
            System.out.println("DAO: Binding username '" + username + "' and timestamp '" + currentTimestampStr + "'"); // Debugging: confirm bindings

            // Set the change IDs for the IN clause
            for (Integer id : changeIds) {
                stmt.setInt(paramIndex++, id);
            }
            System.out.println("DAO: Binding change IDs: " + changeIds); // Debugging: confirm IDs

            int updatedRows = stmt.executeUpdate();
            System.out.println("DAO: Successfully marked " + updatedRows + " changelog entries as read for user: " + username);

        } catch (SQLException e) {
            System.err.println("DAO SQL Error marking changelogs as read for user " + username + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("DAO Unexpected Error marking changelogs as read for user " + username + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}