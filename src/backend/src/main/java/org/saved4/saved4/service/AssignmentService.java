package org.saved4.saved4.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import org.saved4.saved4.dto.Container;
import org.saved4.saved4.dto.Order;
import org.saved4.saved4.dto.Vehicle;
import org.saved4.saved4.enums.ContainerStatus;
import org.saved4.saved4.enums.OrderStatus;
import org.saved4.saved4.enums.VehicleStatus;

@ApplicationScoped
public class AssignmentService {
    @Inject
    private OrderService orderService;
    @Inject
    private VehicleService vehicleService;
    @Inject
    private ContainerService containerService; // Add ContainerService

    /**
     * Checks if a specific vehicle is available for a *continuous slot that ends at or before*
     * the requested 'tw_end'. The 'tw_start' may be adjusted based on existing orders.
     * If available, it returns the adjusted start time, effectively providing the
     * earliest possible start time for a continuous slot ending at tw_end.
     *
     * @param vehicleId      The ID of the vehicle to check.
     * @param requestTwStart The desired start of the time window.
     * @param requestTwEnd   The desired (fixed) end of the time window.
     * @return The Timestamp representing the earliest possible start of a continuous
     * slot ending at requestTwEnd, or null if no such slot exists.
     */
    public Timestamp getAvailableStartTimeForFixedEndTime(Integer vehicleId,
                                                          Timestamp requestTwStart,
                                                          Timestamp requestTwEnd) {
        // --- Input Validation ---
        if (vehicleId == null || requestTwStart == null || requestTwEnd == null) {
            System.err.println("Error: Vehicle ID, start, and end timestamps cannot be null.");
            return null;
        }
        if (requestTwStart.after(requestTwEnd) || requestTwStart.equals(requestTwEnd)) {
            System.err.println("Error: Invalid time window. requestTwStart (" + requestTwStart +
                                       ") must be strictly before requestTwEnd (" + requestTwEnd +
                                       ").");
            return null;
        }

        List<String> statusFilterList = new ArrayList<>();
        statusFilterList.add("created");
        statusFilterList.add("scheduled");

        List<Order> orderObjectList =
                orderService.getFilteredOrders(null, vehicleId, null, null, null, null, null,
                                               statusFilterList, null, null, null, null);

        // If no orders, the vehicle is available from requestTwStart to requestTwEnd
        if (orderObjectList == null || orderObjectList.isEmpty()) {
            return requestTwStart; // Vehicle is free from the requested start
        }

        // Sort orders by their end time to process chronologically
        orderObjectList.sort((o1, o2) -> o1.getTwEnd().compareTo(o2.getTwEnd()));

        Timestamp earliestFreeTime =
                requestTwStart; // This tracks when the vehicle becomes free *before* requestTwEnd

        for (Order order : orderObjectList) {
            // Check if the current order overlaps with the *desired* fixed window
            // The overlap condition needs to be precise for a fixed end time:
            // An order conflicts if it's active *during* any part of the
            // [earliestFreeTime, requestTwEnd] interval.
            boolean conflicts = order.getTwStart().before(requestTwEnd) &&
                    order.getTwEnd().after(earliestFreeTime);

            if (conflicts) {
                // If the order's end time is after our current earliest free time,
                // it means this order pushes out when the vehicle becomes free.
                if (order.getTwEnd().after(earliestFreeTime)) {
                    earliestFreeTime = order.getTwEnd();
                }

                // IMPORTANT: If `earliestFreeTime` moves to or beyond `requestTwEnd`,
                // it means there's no continuous slot ending at `requestTwEnd`.
                if (earliestFreeTime.after(requestTwEnd) || earliestFreeTime.equals(requestTwEnd)) {
                    return null; // Vehicle is occupied right up to or past the desired end time
                }
            }
        }

        // If we reach here, `earliestFreeTime` is the point from which the vehicle is free
        // up to `requestTwEnd`. This is the adjusted start time for the available slot.
        // It must be before requestTwEnd.
        if (earliestFreeTime.before(requestTwEnd)) {
            return earliestFreeTime;
        }

        return null; // Should not be reached if earliestFreeTime.before(requestTwEnd) is true above
    }


    /**
     * Finds and returns information for the first vehicle that has a continuous available slot
     * ending at the specified `tw_end`, starting from `tw_start` or later.
     *
     * @param tw_start The desired start of the time window.
     * @param tw_end   The desired (fixed) end of the time window.
     * @return A Map containing the vehicle ID and a Map of the available time slot
     * (adjusted_start_time -> tw_end) for the first available vehicle found,
     * or null if no vehicle is available.
     */
    public Map<Integer, Map<Timestamp, Timestamp>> getAvailableVehiclesOnATimeWindow(
            Timestamp tw_start, Timestamp tw_end) {
        System.out.println("Checking availability from " + tw_start + " to fixed " + tw_end);

        if (tw_start.after(tw_end) || tw_start.equals(tw_end)) {
            System.err.println("Invalid time window: tw_start must be strictly before tw_end.");
            return null;
        }

        List<Vehicle> vehicleObjectList = vehicleService.getAllVehicles();

        if (vehicleObjectList == null || vehicleObjectList.isEmpty()) {
            System.out.println("No vehicles found in the system.");
            return null;
        }

        for (Vehicle vehicle : vehicleObjectList) {
            // Get the adjusted start time for this vehicle, keeping tw_end fixed
            Timestamp adjustedStartTime =
                    getAvailableStartTimeForFixedEndTime(vehicle.getVehicleId(), tw_start, tw_end);

            if (adjustedStartTime != null) {
                // If adjustedStartTime is not null, it means the vehicle is available
                // from adjustedStartTime until tw_end.
                Map<Timestamp, Timestamp> twStartEndSlots =
                        generateTimeSlots(adjustedStartTime, tw_end);

                // We are looking for *a* slot. If generateTimeSlots yields any slot,
                // then the vehicle is considered available for booking.
                if (!twStartEndSlots.isEmpty()) {
                    // Your original code was taking the LAST entry from generateTimeSlots.
                    // If you want the LAST 30-min slot in the available period:
                    Map.Entry<Timestamp, Timestamp> lastEntry = null;
                    for (Map.Entry<Timestamp, Timestamp> entry : twStartEndSlots.entrySet()) {
                        lastEntry = entry;
                    }

                    if (lastEntry != null) {
                        Map<Integer, Map<Timestamp, Timestamp>> result = new HashMap<>();
                        Map<Timestamp, Timestamp> slot = new HashMap<>();
                        slot.put(lastEntry.getKey(), lastEntry.getValue());
                        result.put(vehicle.getVehicleId(), slot);
                        return result; // Return the first vehicle found with an available slot
                    }
                }
            }
        }

        System.out.println(
                "No available vehicles found for the given time window with fixed end time.");
        return null;
    }


    /**
     * Generate an evenly distributed timeslots (multiple of 30 minutes).
     *
     * @param tw_start
     * @param tw_end
     * @return
     */
    public static Map<Timestamp, Timestamp> generateTimeSlots(Timestamp tw_start,
                                                              Timestamp tw_end) {
        Map<Timestamp, Timestamp> timeSlots = new LinkedHashMap<>();

        if (tw_start == null || tw_end == null || tw_start.after(tw_end)) {
            System.err.println(
                    "Invalid time window: tw_start must be before or equal to tw_end, and neither can be null.");
            return timeSlots; //
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(tw_start.getTime());

        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);
        int milliseconds = calendar.get(Calendar.MILLISECOND);

        if (minutes % 30 != 0 || seconds != 0 || milliseconds != 0) {
            int minutesToAdd = 30 - (minutes % 30);
            if (minutesToAdd == 30 && (seconds != 0 || milliseconds != 0)) {
                calendar.add(Calendar.MINUTE, 30);
            } else if (minutesToAdd != 30) {
                calendar.add(Calendar.MINUTE, minutesToAdd);
            }
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }

        Timestamp currentSlotStart = new Timestamp(calendar.getTimeInMillis());

        while (currentSlotStart.before(tw_end)) {
            calendar.setTimeInMillis(currentSlotStart.getTime());
            calendar.add(Calendar.MINUTE, 30);
            Timestamp currentSlotEnd = new Timestamp(calendar.getTimeInMillis());

            if (currentSlotEnd.after(tw_end)) {
                currentSlotEnd = tw_end;
            }

            timeSlots.put(currentSlotStart, currentSlotEnd);

            currentSlotStart = currentSlotEnd;
        }
        return timeSlots;
    }


    // assign order and vehicle and container
    public Order assignOrderToVehicleAndContainer(Order order, Vehicle vehicle,
                                                  Container container) {

        if (order == null) {
            throw new NotFoundException("Order not found");
        }
        if (vehicle == null) {
            throw new NotFoundException("Vehicle not found");
        }
        if (container == null) {
            throw new NotFoundException("Container not found");
        }

        order.setVehicleId(vehicle.getVehicleId());
        order.setContainerId(container.getContainerId());
        order.setStatus(OrderStatus.scheduled);

        vehicle.setStatus(VehicleStatus.in_use);
        container.setContainerStatus(ContainerStatus.in_use);

        orderService.updateOrder(order.getOrderId(), order);
        vehicleService.updateVehicle(vehicle.getVehicleId(), vehicle);
        containerService.updateContainer(container.getContainerId(), container);

        return order;
    }

    /**
     * Check all the orders that starts after the given twStart.
     *
     * @return list of orders
     */
    public List<Order> retrieveExistingScheduledOrders(Timestamp twStart, Timestamp twEnd) {
        List<String> statusFilterList = new ArrayList<>();
        statusFilterList.add("scheduled");

        List<Order> orderList;
        orderList = orderService.getFilteredOrders(null, null, null, null, null, null, null,
                                                   statusFilterList, null, null, null, null);

        if (orderList == null || orderList.isEmpty()) {
            System.out.println("No orders found in the system.");
            return null;
        }

        return orderList.stream()
                .filter(order -> order.getTwStart() != null && !order.getShared() &&
                        ((twStart.before(order.getTwStart()) && order.getTwEnd().after(twStart) &&
                                twEnd.after(order.getTwEnd())) ||
                                (order.getTwStart().before(twStart) &&
                                        order.getTwEnd().after(twStart) &&
                                        order.getTwEnd().before(twEnd))))
                .collect(Collectors.toList());
    }

    private final BigDecimal MAX_WEIGHT = new BigDecimal("2500.0");

    public Boolean validateForShared(Order order) {
        List<Order> existingOrders = new ArrayList<>();
        existingOrders = retrieveExistingScheduledOrders(order.getTwStart(), order.getTwEnd());

        for (Order existingOrder : existingOrders) {
            //check if order wants to be shared
            if (existingOrder.getPreferredShared()) {
                //            comparing the weight
                if (existingOrder.getFreightWeight().add(order.getFreightWeight())
                        .compareTo(MAX_WEIGHT) < 0) {
                    //                checking if vehicle is available after the tw start or end
                    if (getAvailableStartTimeForFixedEndTime(existingOrder.getVehicleId(),
                                                             order.getTwStart(),
                                                             order.getTwEnd()) != null) {
                        return true;
                    }
                }

            }
        }
        return false;
    }

    public Order orderAssignmentAfterCreated(Order order) {
        System.out.println("[INFO] -- ASSIGNING ORDER with ID: " + order.getOrderId());

        // Checking for all existing
        List<Order> existingOrders =
                retrieveExistingScheduledOrders(order.getTwStart(), order.getTwEnd());

        if (existingOrders != null) {
            System.out.println("true not nulle xisting orders");
            if (!existingOrders.isEmpty()) {
                System.out.println("true not not empty existing orders");
                for (Order existingOrder : existingOrders) {
                    if (order.getPreferredShared() && existingOrder.getPreferredShared()) {
                        System.out.println("both preferred to get shared");
                        if (order.getOperationType().equals(existingOrder.getOperationType())) {
                            System.out.println("same operation type");
                            Boolean canBeShared = validateForShared(order);
                            if (canBeShared) {
                                System.out.println("can be shared");
                                Integer routeId = existingOrder.getRouteId();
                                order.setRouteId(routeId);

                                Timestamp departure = existingOrder.getTwEnd();
                                Timestamp eta = order.getTwEnd();
                                Integer vehicleId = existingOrder.getVehicleId();
                                ;
                                Integer containerId = existingOrder.getContainerId();


                                order = assigningOrderToAttributes(order, vehicleId, containerId,
                                                                   departure, eta,
                                                                   order.getRouteId());
                                order.setShared(true);
                                System.out.println("order: " + order);
                                // update the new order
                                orderService.updateOrder(order.getOrderId(), order);

                                existingOrder.setShared(true);
                                System.out.println("existing: " + existingOrder);
                                orderService.updateOrder(existingOrder.getOrderId(), existingOrder);
                                return order; // stop here if success
                            }
                        }
                    }
                    // both can be shared

                }
            }
        }

        Integer orderRouteId = null;
        //                Inserting new route
        //                if (order.getRouteId() == null) {
        //                    orderRouteId = orderService.insertNewRoute();
        //                    order.setRouteId(orderRouteId);
        //                } else {
        //                    orderRouteId = order.getRouteId();
        //                }

        orderRouteId = orderService.insertNewRoute();

        System.out.println("route id" + orderRouteId);

        if (orderRouteId != null) {
            //            get available vehicles
            Map<Integer, Map<Timestamp, Timestamp>> availableVehiclesOnATimeWindow =
                    getAvailableVehiclesOnATimeWindow(order.getTwStart(), order.getTwEnd());
            if (availableVehiclesOnATimeWindow.isEmpty()) {
                return null; // no vehicle is available
            } else {
                Map.Entry<Integer, Map<Timestamp, Timestamp>> firstEntry =
                        availableVehiclesOnATimeWindow.entrySet().iterator().next();
                Integer vehicleId = firstEntry.getKey();

                Map.Entry<Timestamp, Timestamp> timeWindow =
                        firstEntry.getValue().entrySet().iterator().next();
                Timestamp twStart = timeWindow.getKey();
                Timestamp twEnd = timeWindow.getValue();

                // check container availability
                List<Container> containerList = containerService.getAllContainers();
                for (Container container : containerList) {
                    if (getContainerAvailability(container.getContainerId(), twStart, twEnd)) {
                        //                assigning a vehicle
                        order = assigningOrderToAttributes(order, vehicleId,
                                                           container.getContainerId(), twStart,
                                                           twEnd, orderRouteId);
                        System.out.println("order: " + order);
                        orderService.updateOrder(order.getOrderId(), order); // update order
                        return order;
                    }
                }
                System.out.println("no container is available");
            }

        } else {
            System.out.println("Error inserting route");
        }
        return order;
    }

    // Keep the original assignOrderToVehicle if it's still used elsewhere,
    // or remove it if assignOrderToVehicleAndContainer becomes the primary assignment method.
    public Order assigningOrderToAttributes(Order order, Integer vehicleId, Integer containerId,
                                            Timestamp departure, Timestamp eta, Integer routeId) {
        if (order == null) {
            throw new NotFoundException("Order not found");
        }
        order.setVehicleId(vehicleId);
        order.setStatus(OrderStatus.scheduled);
        order.setEta(eta);
        order.setContainerId(containerId);
        order.setDepartureTime(departure);
        order.setRouteId(routeId);

        return order;
    }


    /**
     * Container allocation.
     *
     * @param containerId
     * @param departureTime
     * @param endTime
     * @return
     */
    public Boolean getContainerAvailability(Integer containerId, Timestamp departureTime,
                                            Timestamp endTime) {
        // --- Input Validation ---
        if (containerId == null || departureTime == null || endTime == null) {
            System.err.println("Error: Container ID, start, and end timestamps cannot be null.");
            return null;
        }
        if (departureTime.after(endTime) || departureTime.equals(endTime)) {
            System.err.println("Error: Invalid time window. requestTwStart (" + departureTime +
                                       ") must be strictly before requestTwEnd (" + endTime + ").");
            return null;
        }

        List<String> statusFilterList = new ArrayList<>();
        statusFilterList.add("created");
        statusFilterList.add("scheduled");

        List<Order> orderObjectList =
                orderService.getFilteredOrders(containerId, null, null, null, null, null, null,
                                               statusFilterList, null, null, null, null);

        // If no orders, the vehicle is available from requestTwStart to requestTwEnd
        if (orderObjectList == null || orderObjectList.isEmpty()) {
            return true; // Container is free from the requested start
        }

        // Sort orders by their end time to process chronologically
        orderObjectList.sort((o1, o2) -> o1.getTwEnd().compareTo(o2.getTwEnd()));

        Timestamp earliestFreeTime =
                departureTime; // This tracks when the container becomes free *before* requestTwEnd

        for (Order order : orderObjectList) {
            // Check if the current order overlaps with the *desired* fixed window
            // The overlap condition needs to be precise for a fixed end time:
            // An order conflicts if it's active *during* any part of the
            // [earliestFreeTime, requestTwEnd] interval.
            boolean conflicts = order.getTwStart().before(departureTime) &&
                    order.getTwEnd().after(earliestFreeTime);

            if (conflicts) {
                // If the order's end time is after our current earliest free time,
                // it means this order pushes out when the vehicle becomes free.
                if (order.getTwEnd().after(earliestFreeTime)) {
                    earliestFreeTime = order.getTwEnd();
                }

                // IMPORTANT: If `earliestFreeTime` moves to or beyond `requestTwEnd`,
                // it means there's no continuous slot ending at `requestTwEnd`.
                if (earliestFreeTime.after(departureTime) || earliestFreeTime.equals(endTime)) {
                    return false; // Vehicle is occupied right up to or past the desired end time
                }
            }
        }

        // If we reach here, `earliestFreeTime` is the point from which the vehicle is free
        // up to `requestTwEnd`. This is the adjusted start time for the available slot.
        // It must be before requestTwEnd.
        if (earliestFreeTime.before(endTime)) {
            return true;
        }

        return null; // Should not be reached if earliestFreeTime.before(requestTwEnd) is true above
    }
}
