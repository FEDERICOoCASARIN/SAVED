package org.saved4.saved4.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.saved4.saved4.dto.Notification;
import org.saved4.saved4.service.NotificationService;

/**
 * RESTful resource class for managing notifications.
 * Provides endpoints to retrieve, update, and delete notifications,
 * as well as mark them as read or unread.
 */
@Path("/notifications")
public class NotificationResource {
    @Inject
    private NotificationService notificationService;

    // Inject ContainerRequestContext directly into the resource
    @Context
    private ContainerRequestContext requestContext;

    public NotificationResource() {

    }

    /**
     * Retrieves all notifications in the system.
     *
     * @param recipient Optional query parameter to filter notifications by target company.
     * @return A Response object containing a list of notifications or a dummy notification if empty.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllNotifications(@QueryParam("recipient") String recipient) {
        System.out.println("Resource: GET /notifications (no receiver)");
        List<Notification> notifications = notificationService.getAllNotifications(recipient);
        System.out.println("Returned from service: " + notifications.size());

        return Response.ok(notifications).build();
    }

    /**
     * Marks a specific notification as read.
     *
     * @param id The ID of the notification to mark as read.
     * @return A Response indicating success or failure of the update operation.
     */
    @POST
    @Path("/{id}/read")
    public Response markAsRead(@PathParam("id") int id) {
        System.out.println("RESOURCE: Marking notification ID " + id + " as read");
        boolean success = notificationService.setReadStatus(id, true);
        System.out.println("RESOURCE: Mark as read success: " + success);
        if (success) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Notification not found")
                    .build();
        }
    }


    /**
     * Marks a specific notification as unread.
     *
     * @param id The ID of the notification to mark as unread.
     * @return A Response indicating success or failure of the update operation.
     */
    @POST
    @Path("/{id}/unread")
    public Response markAsUnread(@PathParam("id") int id) {
        System.out.println("RESOURCE: Marking notification ID " + id + " as unread");
        boolean success = notificationService.setReadStatus(id, false);
        System.out.println("RESOURCE: Mark as unread success: " + success);
        if (success) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Notification not found")
                    .build();
        }
    }


    /**
     * Deletes a specific notification from the system.
     *
     * @param id The ID of the notification to delete.
     * @return A Response indicating whether the deletion was successful.
     */
    @DELETE
    @Path("/{id}")
    public Response deleteNotification(@PathParam("id") int id) {
        boolean success = notificationService.deleteNotification(id);
        System.out.println("Deleting notification ID: " + id);
        if (success) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Notification not found")
                    .build();
        }
    }

}
