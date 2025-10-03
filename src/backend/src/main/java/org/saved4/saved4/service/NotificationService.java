package org.saved4.saved4.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.saved4.saved4.dao.NotificationDao;
import org.saved4.saved4.dto.Notification;
import org.saved4.saved4.enums.NotificationType;
import org.saved4.saved4.enums.Role;

/**
 * Service layer for managing notifications.
 * Handles business logic and delegates database operations to NotificationDao.
 */
@ApplicationScoped
public class NotificationService {

    @Inject
    private NotificationDao notificationDao;

    @Context
    ContainerRequestContext requestContext;

    /**
     * Retrieves all notifications from the database.
     *
     * @return a list of all Notification objects
     */
    public List<Notification> getAllNotifications(String recipient) {
        Role role = (Role) requestContext.getProperty("db_role");
        System.out.println(role);
        return notificationDao.getAllNotifications(role, recipient);
    }


    /**
     * Creates a notification for a newly placed order.
     *
     * @param receiver the username or identifier of the notification recipient
     * @param orderId  the ID of the newly placed order
     */
    public void createNewOrderNotification(String receiver, int orderId) {
        Notification notification = new Notification();
        notification.setReceiver(receiver);
        notification.setOrderId(orderId);
        notification.setTitle("CREATED - Order #" + notification.getOrderId());
        notification.setType(NotificationType.ORDER_CREATION);
        notification.setMessageId(null); // not a message
        notification.setData(
                "New order #" + notification.getOrderId() + " for " + notification.getReceiver() +
                        " has been placed.");
        notification.setRead(false);
        notification.setTimestamp(new Timestamp(System.currentTimeMillis()));

        Role role = (Role) requestContext.getProperty("db_role");
        notificationDao.insertNotification(role, notification);
    }


    /**
     * Creates a notification for an updated order.
     *
     * @param receiver   the username or identifier of the notification recipient
     * @param orderId    the ID of the order that was updated
     * @param modifiedAt the timestamp indicating when the order was modified
     */
    public void createUpdateOrderNotification(String receiver, int orderId,
                                              LocalDateTime modifiedAt) {
        Notification notification = new Notification();
        notification.setReceiver(receiver);
        notification.setOrderId(orderId);
        notification.setTitle("UPDATED - Order #" + notification.getOrderId());
        notification.setType(NotificationType.ORDER_UPDATE);
        notification.setRead(false);
        notification.setTimestamp(new Timestamp(System.currentTimeMillis()));
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm").withLocale(Locale.ENGLISH);
        String formattedDate = modifiedAt.format(formatter);

        StringBuilder details = new StringBuilder();
        details.append("Order #").append(orderId).append(" has been updated.");

        details.append("\nModified at: ").append(formattedDate).append(".");
        details.append("\nSee changelog in orders page for details.");

        notification.setData(details.toString());

        Role role = (Role) requestContext.getProperty("db_role");
        notificationDao.insertNotification(role, notification);
    }

    /**
     * Creates a notification for a deleted order.
     *
     * @param receiver  the username or identifier of the notification recipient
     * @param orderId   the ID of the deleted order
     * @param deletedAt the timestamp indicating when the order was deleted
     */
    public void createDeleteOrderNotification(String receiver, int orderId,
                                              LocalDateTime deletedAt) {
        Notification notification = new Notification();
        notification.setReceiver(receiver);
        notification.setOrderId(orderId);
        notification.setTitle("DELETED - Order #" + notification.getOrderId());
        notification.setType(NotificationType.ORDER_DELETION);
        notification.setRead(false);
        notification.setTimestamp(new Timestamp(System.currentTimeMillis()));
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm").withLocale(Locale.ENGLISH);
        String formattedDate = deletedAt.format(formatter);

        StringBuilder details = new StringBuilder();
        details.append("Order #").append(orderId).append(" has been deleted.");

        details.append("\nDeleted at: ").append(formattedDate).append(".");
        details.append("\nYou can no longer access this order's details.");

        notification.setData(details.toString());

        Role role = (Role) requestContext.getProperty("db_role");
        notificationDao.insertNotification(role, notification);
    }


    /**
     * Updates the read status of a specific notification.
     *
     * @param notificationId the ID of the notification to update
     * @param read           true to mark as read, false to mark as unread
     * @return true if the update was successful, false otherwise
     */
    public boolean setReadStatus(int notificationId, boolean read) {
        System.out.println(
                "SERVICE: Setting read status of notification ID: " + notificationId + " to " +
                        read);
        Role role = (Role) requestContext.getProperty("db_role");
        boolean result = notificationDao.setReadStatus(role, notificationId, read);
        System.out.println("SERVICE: Read status update success: " + result);
        return result;
    }


    /**
     * Deletes a specific notification.
     *
     * @param notificationId the ID of the notification to delete
     * @return true if the deletion was successful, false otherwise
     */
    public boolean deleteNotification(int notificationId) {
        System.out.println("SERVICE: Deleting notification ID: " + notificationId);
        Role role = (Role) requestContext.getProperty("db_role");
        boolean result = notificationDao.deleteNotification(role, notificationId);
        System.out.println("SERVICE: Deletion success: " + result);
        return result;
    }
}
