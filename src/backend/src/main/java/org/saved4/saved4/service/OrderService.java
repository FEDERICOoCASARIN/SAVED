package org.saved4.saved4.service;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.postgresql.geometric.PGpoint;
import org.saved4.saved4.dao.ChangeLogDao;
import org.saved4.saved4.dao.CompanyDao;
import org.saved4.saved4.dao.OrderDao;
import org.saved4.saved4.dto.*;
import org.saved4.saved4.enums.ContainerStatus;
import org.saved4.saved4.enums.OrderStatus;
import org.saved4.saved4.enums.Role;
import org.saved4.saved4.enums.VehicleStatus;
//import java.util.function.BiConsumer;

@RequestScoped
public class OrderService {
    @Inject
    private OrderDao orderDao;
    @Inject
    private AssignmentService assignmentService;
    @Inject
    private VehicleService vehicleService;
    @Inject
    private ContainerService containerService;
    @Inject
    private NotificationService notificationService;

    @Context
    ContainerRequestContext requestContext;
    @Inject
    private CompanyDao companyDao;

    public static final PGpoint PORT = new PGpoint(4.4338, 51.9515);

    /**
     * Retrieves a list of all orders from the database.
     *
     * @return A list of all orders from DB.
     */
    public List<Order> getAllOrders() {
        Role role = (Role) requestContext.getProperty("db_role");
        return orderDao.getAllOrdersDB(role);
    }

    /**
     * Retrieves a specific order by its ID and an optional order for.
     * The order_for parameter will be used to provide orders that are intended to the recipient
     * (used in search and filter).
     *
     * @param id        The unique identifier of the order.
     * @param order_for An optional string specifying the recipient of order.
     * @return The order object corresponding to the given ID, or null if not found.
     */
    public Order getOrderById(int id, String order_for) {
        Role role = (Role) requestContext.getProperty("db_role");
        String dbRequester = (String) requestContext.getProperty("username");
        System.out.println("get order by id");
        return orderDao.getOrderByIdDB(role, dbRequester, id, order_for);
    }

    /**
     * Adds a new order to the database.
     *
     * @param order The order object to be added.
     * @return The created order object with its generated ID, or null if creation failed.
     */
    public Order addNewOrder(Order order) {
        Role role = (Role) requestContext.getProperty("db_role");
        String dbRequester = (String) requestContext.getProperty("username");
        System.out.println("adding new order");

        Order createdOrder = orderDao.insertOrderDB(role, dbRequester, order);
        System.out.println("Add new order service");

        if (createdOrder != null) {
            try {
                triggerOrderNotification(createdOrder, "create", null);
            } catch (Exception e) {
                System.err.println("Notification creation failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return createdOrder;
    }


    /**
     * Updates an existing order in the database.
     * This method also tracks changes between the original and updated order
     * and logs these changes into a change log object.
     *
     * @param id           The unique identifier of the order to be updated.
     * @param updatedOrder The order object containing the updated information.
     * @return The updated order object, or null if the update failed or the order was not found.
     * @throws RuntimeException if the order with the given ID is not found.
     */
    public Order updateOrder(int id, Order updatedOrder) {
        Role role = (Role) requestContext.getProperty("db_role");
        String dbRequester = (String) requestContext.getProperty("username");
        ChangeLogDao changeLogDao = new ChangeLogDao();
        Order originalOrder = orderDao.getOrderByIdDB(role, dbRequester, id, null);

        if (originalOrder == null) {
            throw new RuntimeException("Order not found with id " + id);
        }

        Map<String, Object> dataInitial = new HashMap<>();
        Map<String, Object> dataChanged = new HashMap<>();
        List<String> changedFields =
                new ArrayList<>(); // Use List to maintain order if desired, or Set for uniqueness

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX");
        ZoneId systemZone = ZoneId.systemDefault();

        for (Field field : Order.class.getDeclaredFields()) {
            if (field.getName().equals("orderId") || field.getName().equals("sourcePosition") ||
                    field.getName().equals("destinationPosition")) {
                continue;
            }

            field.setAccessible(true);

            try {
                Object originalVal = field.get(originalOrder);
                Object updatedVal = field.get(updatedOrder);

                processFieldChange(field.getName(), originalVal, updatedVal, dataInitial,
                                   dataChanged, changedFields, formatter, systemZone);
            } catch (IllegalAccessException e) {
                System.err.println(
                        "Error accessing field " + field.getName() + " using reflection: " +
                                e.getMessage());
            }
        }

        if (!changedFields.isEmpty()) {
            ChangeLog changeLog = new ChangeLog();
            changeLog.setOrderId(id);
            changeLog.setModifiedAt(new Timestamp(System.currentTimeMillis()));
            changeLog.setModifiedBy(originalOrder.getRequester());
            changeLog.setOrderOwner(originalOrder.getRequester());
            changeLog.setTypeChanged(changedFields);
            changeLog.setDataInitial(dataInitial);
            changeLog.setDataChanged(dataChanged);

            List<ChangeStatus> detectedStatuses = new ArrayList<>();
            Set<String> changedFieldsSet =
                    new HashSet<>(changedFields); // Use a Set for efficient lookup

            // Priority 1: REMOVED (if only status is CANCELED)
            if (changedFieldsSet.size() == 1 && changedFieldsSet.contains("status")) {
                Object updatedStatusObj = dataChanged.get("status");
                if (updatedStatusObj != null &&
                        updatedStatusObj.toString().equalsIgnoreCase("CANCELED")) {
                    detectedStatuses.add(ChangeStatus.removed);
                }
            }

            // If not REMOVED, check for RESCHEDULED and EDITED
            if (detectedStatuses.isEmpty()) { // Only proceed if it's not a 'removed' event
                // Check for RESCHEDULED
                if (changedFieldsSet.contains("twStart") || changedFieldsSet.contains("twEnd")) {
                    detectedStatuses.add(ChangeStatus.rescheduled);

                    if (updatedOrder.getStatus().equals(OrderStatus.scheduled)) {
                        updatedOrder.setStatus(OrderStatus.created);
                        updatedOrder.setContainerId(null);
                        updatedOrder.setVehicleId(null);

                        if (updatedOrder.getShared()) {
                            List<Order> listOrder =
                                    getFilteredOrders(null, null, null, null, null, null, null,
                                                      null, null, null, null,
                                                      updatedOrder.getRouteId());
                            for (Order order : listOrder) {
                                if (order.getOrderId() != updatedOrder.getOrderId()) {
                                    order.setShared(false);

                                    orderDao.updateOrderDB(role, dbRequester, order.getOrderId(),
                                                           order);
                                }
                            }
                        }
                        updatedOrder.setRouteId(null);
                        updatedOrder.setShared(false);
                    }
                }

                //                if (changedFieldsSet.contains("eta") || changedFieldsSet.contains("etd")) {
                //                    detectedStatuses.add(ChangeStatus.rescheduled);
                //                }


                // Check for EDITED (for freight details, or general fallback)
                // This will add EDITED if any freight fields change, or if other fields change
                // and RESCHEDULED wasn't already added.
                if (changedFieldsSet.contains("freightType") ||
                        changedFieldsSet.contains("freightWeight") ||
                        changedFieldsSet.contains("freightValue")) {
                    detectedStatuses.add(ChangeStatus.edited);
                }

                // Fallback: If no specific categories were hit, but there are changes, mark as EDITED.
                // This ensures any other field changes still result in an 'edited' status.
                if (detectedStatuses.isEmpty() && !changedFields.isEmpty()) {
                    detectedStatuses.add(ChangeStatus.edited);
                }
            }

            // Set the detected statuses (will be converted to comma-separated string)
            changeLog.setChangeStatus(detectedStatuses);
            // --- END NEW LOGIC ---

            try {
                changeLogDao.insertChangeLog(changeLog);

            } catch (Exception e) {
                System.err.println("Failed to insert changelog: " + e.getMessage());
                e.printStackTrace();
            }
            try {
                System.out.println("[DEBUG] Changed fields: " + changedFields);
                System.out.println("[DEBUG] Updated order status: " + updatedOrder.getStatus());
                System.out.println("[DEBUG] Order ID: " + updatedOrder.getOrderId());
                System.out.println("[DEBUG] Order For: " + updatedOrder.getOrder_for());
                System.out.println("[DEBUG] Shared: " + updatedOrder.getShared());

                if (updatedOrder == null) {
                    triggerOrderNotification(updatedOrder, "deletion", LocalDateTime.now());
                } else if (!(updatedOrder.getStatus() == OrderStatus.created)) {
                    System.out.println("[DEBUG] Triggering update notification for order: " +
                                               updatedOrder.getOrderId());
                    triggerOrderNotification(updatedOrder, "update", LocalDateTime.now());
                } else {
                    System.out.println(
                            "[DEBUG] Skipped update notification because status is 'created'");
                }
            } catch (Exception e) {
                System.err.println("Notification creation failed: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return orderDao.updateOrderDB(role, dbRequester, id, updatedOrder);
    }

    /**
     * Helper method to compare two values, detect changes, and populate changelog data.
     * Handles different data types including Timestamps, LocalDateTimes, Enums, and Numbers.
     *
     * @param fieldName     The name of the field being compared.
     * @param originalVal   The original value of the field.
     * @param updatedVal    The updated value of the field.
     * @param dataInitial   Map to store initial values of changed fields.
     * @param dataChanged   Map to store updated values of changed fields.
     * @param changedFields List to store names of changed fields.
     * @param formatter     DateTimeFormatter for date/time formatting.
     * @param systemZone    ZoneId for date/time formatting.
     */
    private void processFieldChange(String fieldName, Object originalVal, Object updatedVal,
                                    Map<String, Object> dataInitial,
                                    Map<String, Object> dataChanged, List<String> changedFields,
                                    DateTimeFormatter formatter, ZoneId systemZone) {
        boolean changed = false;
        String originalFormatted = null;
        String updatedFormatted = null;

        if (originalVal == null && updatedVal != null) {
            changed = true;
        } else if (originalVal != null && updatedVal == null) {
            changed = true;
        } else if (originalVal != null) {
            if (originalVal instanceof Timestamp && updatedVal instanceof Timestamp) {
                changed = ((Timestamp) originalVal).getTime() != ((Timestamp) updatedVal).getTime();
                originalFormatted =
                        ZonedDateTime.of(((Timestamp) originalVal).toLocalDateTime(), systemZone)
                                .format(formatter);
                updatedFormatted =
                        ZonedDateTime.of(((Timestamp) updatedVal).toLocalDateTime(), systemZone)
                                .format(formatter);
            } else if (originalVal instanceof LocalDateTime &&
                    updatedVal instanceof LocalDateTime) {
                changed = !((LocalDateTime) originalVal).isEqual((LocalDateTime) updatedVal);
                originalFormatted =
                        ZonedDateTime.of((LocalDateTime) originalVal, systemZone).format(formatter);
                updatedFormatted =
                        ZonedDateTime.of((LocalDateTime) updatedVal, systemZone).format(formatter);
            } else if (originalVal instanceof Enum && updatedVal instanceof Enum) {
                changed = !originalVal.toString().equals(updatedVal.toString());
                originalFormatted = originalVal.toString();
                updatedFormatted = updatedVal.toString();
            } else if (originalVal instanceof Number && updatedVal instanceof Number) {
                changed =
                        ((Number) originalVal).doubleValue() != ((Number) updatedVal).doubleValue();
                originalFormatted = originalVal.toString();
                updatedFormatted = updatedVal.toString();
            } else {
                changed = !originalVal.equals(updatedVal);
                originalFormatted = originalVal.toString();
                updatedFormatted = updatedVal.toString();
            }
        }

        if (changed) {
            changedFields.add(fieldName);
            dataInitial.put(fieldName, originalFormatted != null ? originalFormatted :
                    (originalVal != null ? originalVal.toString() : null));
            dataChanged.put(fieldName, updatedFormatted != null ? updatedFormatted :
                    (updatedVal != null ? updatedVal.toString() : null));
        }
    }


    /**
     * Deletes an order from the database by its ID.
     *
     * @param id The unique identifier of the order to be deleted.
     * @return true if the order was successfully removed, false otherwise.
     */
    public boolean deleteOrder(int id) {
        Role role = (Role) requestContext.getProperty("db_role");
        String dbRequester = (String) requestContext.getProperty("username");

        System.out.println("[DEBUG] Attempting to delete order with ID: " + id);

        Order orderToDelete = getOrderById(id, null);
        if (orderToDelete == null) {
            System.out.println("[DEBUG] Order not found with ID: " + id);
        } else {
            System.out.println("[DEBUG] Order found: " + orderToDelete);
            try {
                triggerOrderNotification(orderToDelete, "deletion", LocalDateTime.now());
            } catch (Exception e) {
                System.err.println("Notification creation failed: " + e.getMessage());
                e.printStackTrace();
            }
        }

        boolean result = orderDao.removeOrderDB(role, dbRequester, id);
        System.out.println("[DEBUG] Order deletion result for ID " + id + ": " + result);
        return result;
    }


    /**
     * Retrieves a list of orders based on various filtering criteria.
     * All parameters are optional and can be null to indicate no filtering on that
     * particular criteria.
     *
     * @param containerId   Filters by the ID of the container associated with the order.
     * @param vehicleId     Filters by the ID of the vehicle associated with the order.
     * @param requester     Filters by the requester's name.
     * @param startDate     Filters orders starting on or after this date
     * @param endDate       Filters orders ending on or before this date
     * @param source        Filters by the source location of the order.
     * @param destination   Filters by the destination location of the order.
     * @param status        Filters by a list of possible order statuses.
     * @param operationType Filters by the type of operation (e.g., "loading", "unloading").
     * @param order_for     Filters by the recipient of the order.
     * @param is_shared     Filters orders based on whether they are shared.
     * @return A list of order objects that match the specified filters.
     */
    public List<Order> getFilteredOrders(Integer containerId, Integer vehicleId, String requester,
                                         String startDate, String endDate, String source,
                                         String destination, List<String> status,
                                         String operationType, String order_for, Boolean is_shared,
                                         Integer routeId) {
        Role role = (Role) requestContext.getProperty("db_role");
        String dbRequester = (String) requestContext.getProperty("username");
        return orderDao.getFilteredOrders(role, dbRequester, containerId, vehicleId, requester,
                                          startDate, endDate, source, destination, status,
                                          operationType, order_for, is_shared, routeId);
    }


    /**
     * Triggers a notification based on the action type related to an order.
     *
     * @param order      The Order object involved in the event. May be null if the order was deleted.
     * @param actionType The type of action performed ("create", "update", or "deletion").
     * @param modifiedAt The timestamp at which the action occurred.
     */
    private void triggerOrderNotification(Order order, String actionType,
                                          LocalDateTime modifiedAt) {
        if (order == null) {
            if ("deletion".equalsIgnoreCase(actionType)) {
                System.out.println("Triggering notification: deletion");
                // You can optionally pass the order ID separately if it's retrievable before deletion
                return;
            } else {
                System.err.println(
                        "Order is null. Cannot trigger notification for action: " + actionType);
                return;
            }
        }

        System.out.println(
                "Triggering notification: " + actionType + " for order ID: " + order.getOrderId());

        switch (actionType.toLowerCase()) {
            case "create":
                notificationService.createNewOrderNotification(order.getOrder_for(),
                                                               order.getOrderId());
                break;
            case "update":
                notificationService.createUpdateOrderNotification(order.getOrder_for(),
                                                                  order.getOrderId(), modifiedAt);
                break;
            case "deletion":
                notificationService.createDeleteOrderNotification(order.getOrder_for(),
                                                                  order.getOrderId(), modifiedAt);
                break;
            default:
                System.err.println("Unknown action type: " + actionType);
                break;
        }
    }


    /**
     * Exports orders based on a specified order type, with a default limit of 100 orders.
     *
     * @param order_for The specific purpose or type of the orders to export.
     * @return A list of order objects matching the order_for criterion.
     */
    public List<Order> exportOrders(String order_for) {
        Role role = (Role) requestContext.getProperty("db_role");
        String dbRequester = (String) requestContext.getProperty("username");
        return orderDao.getFilteredOrders(role, dbRequester, null, null, null, null, null, null,
                                          null, null, null, order_for, null, null);
    }

    /**
     * Temporary service.
     */
    public Integer insertNewRoute() {
        Role role = (Role) requestContext.getProperty("db_role");

        return orderDao.insertRouteDB(role);
    }


    /**
     * Activate Order.
     */
    public boolean activateOrder() {
        Role role = (Role) requestContext.getProperty("db_role");
        List<Order> allOrderList = orderDao.getAllOrdersDB(role);
        long timestamp = Instant.now().toEpochMilli();
        Timestamp sqlTimestamp = new Timestamp(timestamp);


        for (Order order : allOrderList) {
            if (order.getStatus() == OrderStatus.scheduled &&
                    sqlTimestamp.after(order.getDepartureTime())) {
                System.out.println("starting order");

                startOrder(order);
            } else if (order.getStatus() == OrderStatus.undergoing &&
                    sqlTimestamp.after(order.getEta())) {

                finishOrder(order);

            } else if (order.getStatus().equals(OrderStatus.created)) {
                System.out.println(order.getStatus());
                System.out.println("created order");
                assignmentService.orderAssignmentAfterCreated(order);

            } else if (order.getStatus() == OrderStatus.canceled &&
                    order.getDepartureTime().before(sqlTimestamp) &&
                    order.getEta().after(sqlTimestamp)) {
                System.out.println("status" + order.getStatus());

                Vehicle vehicle = vehicleService.getVehicleByID(order.getVehicleId());
                stopVehicle(vehicle);
            }
        }

        return true;
    }

    /**
     * Start order if the current time is already after the TW_START.
     *
     * @param order
     */
    public void startOrder(Order order) {
        Role role = (Role) requestContext.getProperty("db_role");
        String dbRequester = (String) requestContext.getProperty("username");
        order.setStatus(OrderStatus.undergoing);

        if (order.getVehicleId() != null) {
            Vehicle vehicle = vehicleService.getVehicleByID(order.getVehicleId());
            vehicle.setStatus(VehicleStatus.in_use);
            vehicleService.updateVehicle(vehicle.getVehicleId(), vehicle);
        }


        //         now we dont have container allocation yet
        if (order.getContainerId() != null) {
            Container container = containerService.getContainerById(order.getContainerId());
            System.out.println(container.getContainerId());
            container.setContainerStatus(ContainerStatus.in_use);
            containerService.updateContainer(container.getContainerId(), container);
        }

        orderDao.updateOrderDB(role, dbRequester, order.getOrderId(), order);

    }


    /**
     * Finish order if the current time is already after the TW_END.
     *
     * @param order
     */
    public void finishOrder(Order order) {
        Role role = (Role) requestContext.getProperty("db_role");
        String dbRequester = (String) requestContext.getProperty("username");

        order.setStatus(OrderStatus.completed);

        // Set company location to vehicle location
        try {
            PGpoint newLocation;

            Company company = companyDao.getByUsername(role, dbRequester, order.getDestination());
            if (company != null) { //not type of company (port)
                newLocation = company.getLocation();
            } else {
                newLocation = PORT;
            }

            if (order.getVehicleId() != null) {
                Vehicle vehicle = vehicleService.getVehicleByID(order.getVehicleId());
                vehicle.setStatus(VehicleStatus.available);
                vehicle.setPosition(newLocation);

                vehicleService.updateVehicle(vehicle.getVehicleId(), vehicle);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        if (order.getContainerId() != null) {
            Container container = containerService.getContainerById(order.getContainerId());
            container.setContainerStatus(ContainerStatus.available);
            containerService.updateContainer(container.getContainerId(), container);
        }

        orderDao.updateOrderDB(role, dbRequester, order.getOrderId(), order);
    }

    /**
     * Stop the operation of the current vehicle.
     *
     * @param vehicle, vehicle object that is about to be updated.
     */
    public void stopVehicle(Vehicle vehicle) {
        vehicle.setStatus(VehicleStatus.available);
        vehicleService.updateVehicle(vehicle.getVehicleId(), vehicle);
    }


    /**
     * Cancels all orders for a specific user that haven't started yet.
     * Only cancels orders with status 'created' or 'scheduled'.
     *
     * @param username The username of the user whose orders should be canceled
     * @return The number of orders that were canceled
     */
    public int cancelOrdersForUser(String username) {
        Role role = (Role) requestContext.getProperty("db_role");
        String dbRequester = (String) requestContext.getProperty("username");

        // Get all orders for the user that haven't started
        List<Order> ordersToCancel = orderDao.getOrdersForUserByStatus(username, Arrays.asList(
                OrderStatus.created, OrderStatus.scheduled));

        int canceledCount = 0;
        for (Order order : ordersToCancel) {
            try {
                // Update order status to canceled
                order.setStatus(OrderStatus.canceled);
                orderDao.updateOrderDB(role, dbRequester, order.getOrderId(), order);

                // Release any assigned vehicle
                if (order.getVehicleId() != null) {
                    Vehicle vehicle = vehicleService.getVehicleByID(order.getVehicleId());
                    if (vehicle != null && vehicle.getStatus() == VehicleStatus.in_use) {
                        vehicle.setStatus(VehicleStatus.available);
                        vehicleService.updateVehicle(vehicle.getVehicleId(), vehicle);
                    }
                }

                // Release any assigned container
                if (order.getContainerId() != null) {
                    Container container = containerService.getContainerById(order.getContainerId());
                    if (container != null &&
                            container.getContainerStatus() == ContainerStatus.in_use) {
                        container.setContainerStatus(ContainerStatus.available);
                        containerService.updateContainer(container.getContainerId(), container);
                    }
                }

                canceledCount++;
            } catch (Exception e) {
                System.err.println(
                        "Failed to cancel order " + order.getOrderId() + " for user " + username +
                                ": " + e.getMessage());
                // Continue with other orders even if one fails
            }
        }

        return canceledCount;
    }
}
