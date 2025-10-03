package org.saved4.saved4.dao;

import jakarta.inject.Singleton;
import org.saved4.saved4.DBProvider;
import org.saved4.saved4.dto.Notification;
import org.saved4.saved4.enums.NotificationType;
import org.saved4.saved4.enums.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class NotificationDao {

    /**
     * Retrieves all notifications from the database, ordered by timestamp in descending order.
     *
     * @return a list of all Notification objects
     */
    public List<Notification> getAllNotifications(Role role, String recipient) {
        List<Notification> notifications = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("""
                SELECT notification_id, data, receiver, order_id, message_id, read, timestamp, type, title
                FROM notifications
                """);

        if (recipient != null && !recipient.trim().isEmpty()) {
            sqlBuilder.append(" WHERE receiver = ?");
        }
        sqlBuilder.append(" ORDER BY timestamp DESC");

        try (Connection conn = DBProvider.getConnection(role, recipient);
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {

            if (recipient != null && !recipient.trim().isEmpty()) {
                stmt.setString(1, recipient);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Integer orderId = rs.getObject("order_id") != null ? rs.getInt("order_id") : null;
                    Integer messageId = rs.getObject("message_id") != null ? rs.getInt("message_id") : null;

                    String typeStr = rs.getString("type");
                    NotificationType type = typeStr != null ? NotificationType.valueOf(typeStr.toUpperCase()) : NotificationType.MISCELLANEOUS;

                    Notification notification = new Notification(
                            rs.getInt("notification_id"),
                            rs.getString("title"),
                            type,
                            rs.getString("data"),
                            rs.getString("receiver"),
                            orderId,
                            messageId,
                            rs.getBoolean("read"),
                            rs.getTimestamp("timestamp")
                    );
                    notifications.add(notification);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving notifications: " + e.getMessage());
            // Consider throwing a custom exception here or handling it more robustly
        }
        return notifications;
    }

    /**
     * Inserts a new notification into the database.
     *
     * @param n the Notification object to insert
     * @return true if the insertion was successful, false otherwise
     */
    public boolean insertNotification(Role role, Notification n) {
        String insertQuery = "INSERT INTO notifications (receiver, order_id, message_id, type, title, data, read, timestamp) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        int affectedRows = 0;
        try (Connection conn = DBProvider.getConnection(role)) {
            conn.setAutoCommit(false);

            if (n.getReceiver() == null || n.getReceiver().isEmpty()) {
                throw new SQLException("Notification receiver cannot be null or empty.");
            }
            if (n.getData() == null || n.getData().isEmpty()) {
                throw new SQLException("Notification data cannot be null or empty.");
            }

            try (PreparedStatement ps = conn.prepareStatement(insertQuery)) {
                ps.setString(1, n.getReceiver());
                ps.setObject(2, n.getOrderId(), Types.INTEGER);
                ps.setObject(3, n.getMessageId(), Types.INTEGER);
                ps.setObject(4, n.getType().name().toLowerCase(), Types.OTHER);
                ps.setString(5, n.getTitle());
                ps.setString(6, n.getData());
                ps.setBoolean(7, n.isRead());
                ps.setTimestamp(8, n.getTimestamp() != null ? n.getTimestamp() : new Timestamp(System.currentTimeMillis()));

                System.out.println("Inserting notification for: " + n.getReceiver() + ", order ID: " + n.getOrderId());
                affectedRows = ps.executeUpdate();
                System.out.println("Inserted notification rows: " + affectedRows);
            }

            conn.commit();
            System.out.println("Notification insert committed.");
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting notification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates the read status of a notification by its ID.
     *
     * @param role           the role to use for database connection
     * @param notificationId the ID of the notification
     * @param read           the new read status
     * @return true if the update was successful, false otherwise
     */
    public boolean setReadStatus(Role role, int notificationId, boolean read) {
        String query = "UPDATE notifications SET read = ? WHERE notification_id = ?";
        try (Connection conn = DBProvider.getConnection(role);
             PreparedStatement ps = conn.prepareStatement(query)) {
            conn.setAutoCommit(false); // manually control the commit
            ps.setBoolean(1, read);
            ps.setInt(2, notificationId);
            boolean success = ps.executeUpdate() > 0;
            conn.commit(); // <- COMMIT HERE
            return success;
        } catch (SQLException e) {
            System.err.println("Error updating read status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a notification from the database by its ID.
     *
     * @param role           the role to use for database connection
     * @param notificationId the ID of the notification to delete
     * @return true if the deletion was successful, false otherwise
     */
    public boolean deleteNotification(Role role, int notificationId) {
        String query = "DELETE FROM notifications WHERE notification_id = ?";
        try (Connection conn = DBProvider.getConnection(role);
             PreparedStatement ps = conn.prepareStatement(query)) {
            conn.setAutoCommit(false);
            ps.setInt(1, notificationId);
            boolean success = ps.executeUpdate() > 0;
            conn.commit();
            return success;
        } catch (SQLException e) {
            System.err.println("Error deleting notification: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves notifications from the database for a specific receiver, ordered by timestamp in descending order.
     *
     * @param role     the role to use for database connection
     * @param receiver the username of the notification recipient
     * @return a list of Notification objects belonging to the receiver
     */
    public List<Notification> getNotifications(Role role, String receiver) {
        List<Notification> notifications = new ArrayList<>();
        String sql = """
                SELECT notification_id, data, receiver, order_id, message_id, read, timestamp, type, title
                FROM notifications
                WHERE receiver = ?
                ORDER BY timestamp DESC
                """;

        try (Connection conn = DBProvider.getConnection(role);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, receiver);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Integer orderId = rs.getObject("order_id") != null ? rs.getInt("order_id") : null;
                    Integer messageId = rs.getObject("message_id") != null ? rs.getInt("message_id") : null;

                    String typeStr = rs.getString("type");
                    NotificationType type = typeStr != null ? NotificationType.valueOf(typeStr.toUpperCase()) : NotificationType.MISCELLANEOUS;

                    Notification notification = new Notification(
                            rs.getInt("notification_id"),
                            rs.getString("title"),
                            type,
                            rs.getString("data"),
                            rs.getString("receiver"),
                            orderId,
                            messageId,
                            rs.getBoolean("read"),
                            rs.getTimestamp("timestamp")
                    );
                    notifications.add(notification);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving notifications for receiver: " + e.getMessage());
        }
        return notifications;
    }
}
