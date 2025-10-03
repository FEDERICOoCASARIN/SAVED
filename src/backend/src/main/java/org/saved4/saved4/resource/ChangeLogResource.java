package org.saved4.saved4.resource;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.saved4.saved4.dto.ChangeLog;
import org.saved4.saved4.service.ChangeLogService;

import java.util.List;
import java.util.Map;

/**
 * RESTful resource for ChangeLog operations.
 * Provides endpoints for retrieving and adding change log entries.
 */
@Path("/changelog")
@RequestScoped
public class ChangeLogResource {

    @Inject
    private ChangeLogService changeLogService;

    /**
     * Constructs a new ChangeLogResource and initializes the ChangeLogService.
     */
    public ChangeLogResource() {
        this.changeLogService = new ChangeLogService();
    }

    /**
     * Retrieves change logs, with optional filtering by order owner.
     * If the 'orderOwner' query parameter is provided, it filters logs for that specific owner.
     * If 'orderOwner' is null or empty, it retrieves all change logs (intended for admin users).
     *
     * @param orderOwner An optional query parameter specifying the ID of the order owner (e.g., company name or user ID)
     *                   to filter the change logs. If this parameter is null or empty, all logs are returned.
     * @return A {@link List} of {@link ChangeLog} objects.
     * @throws WebApplicationException If an error occurs during retrieval (e.g., database error).
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ChangeLog> listChangeLogs(@QueryParam("orderOwner") String orderOwner) {
        try {
            List<ChangeLog> logs;
            if (orderOwner != null && !orderOwner.trim().isEmpty()) {
                // If orderOwner is provided, fetch logs for that specific owner
                logs = changeLogService.getChangeLogsByOrderOwner(orderOwner.trim());
                System.out.println("Resource: Retrieved change logs for order owner " + orderOwner + ". Count: " + logs.size());
            } else {
                // If orderOwner is null or empty (admin view), fetch ALL logs
                logs = changeLogService.getAllChangeLogs();
                System.out.println("Resource: Retrieved all change logs (admin view). Count: " + logs.size());
            }
            return logs;
        } catch (Exception e) {
            System.err.println("Resource Error retrieving change logs: " + e.getMessage());
            e.printStackTrace();
            throw new WebApplicationException("Failed to retrieve change logs: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Adds a new change log entry.
     *
     * @param log The {@link ChangeLog} object containing the data for the new entry.
     * @return A {@link Response} indicating the status of the operation.
     * Returns 201 Created if successful, or 500 Internal Server Error if an exception occurs.
     * @throws WebApplicationException If an error occurs during insertion.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addChangeLog(ChangeLog log) {
        try {
            changeLogService.createChangeLog(log);
            System.out.println("Resource: Successfully inserted new change log for Order ID: " + log.getOrderId());
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            System.err.println("Resource Error calling ChangeLogService.createChangeLog(): " + e.getMessage());
            e.printStackTrace();
            throw new WebApplicationException("Failed to insert change log: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Marks specified change log entries as read by a given user.
     * <p>
     * This endpoint expects a JSON payload like:
     * {
     * "logIds": [1, 5, 10],
     * "username": "currentUser"
     * }
     *
     * @param requestBody A Map containing "logIds" (List of Integers) and "username" (String).
     * @return A {@link Response} indicating the status of the operation.
     * Returns 200 OK if successful, or 400 Bad Request/500 Internal Server Error if an exception occurs.
     * @throws WebApplicationException If the input is invalid or a server error occurs.
     */
    @POST
    @Path("/mark-as-read")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response markLogsAsRead(Map<String, Object> requestBody) {
        try {
            // Debugging: Print incoming request body
            System.out.println("Resource: Received markLogsAsRead request body: " + requestBody);

            List<Integer> logIds = (List<Integer>) requestBody.get("logIds");
            String username = (String) requestBody.get("username");

            if (logIds == null || logIds.isEmpty()) {
                System.err.println("Resource: markLogsAsRead - Missing or empty 'logIds' in request body.");
                throw new WebApplicationException("'logIds' cannot be null or empty.", Response.Status.BAD_REQUEST);
            }
            if (username == null || username.trim().isEmpty()) {
                System.err.println("Resource: markLogsAsRead - Missing or empty 'username' in request body.");
                throw new WebApplicationException("'username' cannot be null or empty.", Response.Status.BAD_REQUEST);
            }

            changeLogService.markLogsAsRead(logIds, username);
            System.out.println("Resource: Successfully marked change logs as read for user: " + username);
            return Response.ok().build(); // 200 OK
        } catch (ClassCastException e) {
            System.err.println("Resource Error: Invalid request body format for markLogsAsRead. Ensure 'logIds' is a list of numbers and 'username' is a string. Error: " + e.getMessage());
            e.printStackTrace();
            throw new WebApplicationException("Invalid request format. Ensure 'logIds' is a list of numbers and 'username' is a string.",
                    Response.Status.BAD_REQUEST);
        } catch (WebApplicationException e) {
            // Re-throw WebApplicationException as it's already properly formed
            throw e;
        } catch (Exception e) {
            System.err.println("Resource Error calling ChangeLogService.markLogsAsRead(): " + e.getMessage());
            e.printStackTrace();
            throw new WebApplicationException("Failed to mark change logs as read: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}