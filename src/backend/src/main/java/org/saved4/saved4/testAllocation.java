package org.saved4.saved4;

import java.sql.Timestamp;
import org.saved4.saved4.dto.Order;
import org.saved4.saved4.service.AssignmentService;
import org.saved4.saved4.service.ContainerService;
import org.saved4.saved4.service.OrderService;
import org.saved4.saved4.service.VehicleService;

public class testAllocation {
    private static OrderService orderService;
    private VehicleService vehicleService;
    private ContainerService containerService;
    private AssignmentService assignmentService;

    public testAllocation() {
        orderService = new OrderService();
        vehicleService = new VehicleService();
        containerService = new ContainerService();
        assignmentService = new AssignmentService();
    }

    public void getVehicleFromAssignment(Timestamp tw_start, Timestamp tw_end) {
        System.out.println("returning from assignment id: " +
                                   this.assignmentService.getAvailableVehiclesOnATimeWindow(
                                           tw_start, tw_end));
    }

    public void getAllScheduledOrders(Timestamp tw_start, Timestamp tw_end) {
        System.out.println("returning from assignment id: " +
                                   this.assignmentService.retrieveExistingScheduledOrders(tw_start,
                                                                                          tw_end));
    }

    public Order assignAtFirst(Order order) {
        Order editedOrder = assignmentService.orderAssignmentAfterCreated(order);
        System.out.println(editedOrder.toString());
        return null;
    }


    public void getContainerAssignment(Integer containerId, Timestamp tw_start, Timestamp tw_end) {
        System.out.println("returning from assignment id: " +
                                   this.assignmentService.getContainerAvailability(containerId,
                                                                                   tw_start,
                                                                                   tw_end));
    }

    public static void main(String[] args) {
        testAllocation test = new testAllocation();
        Timestamp tw_start = Timestamp.valueOf("2025-06-27 17:00:00");
        Timestamp tw_end = Timestamp.valueOf("2025-06-27 19:00:00");
        System.out.println("true");


        //        Order order = orderService.getOrderById(73, null);

        //        test.assignAtFirst(order);]
        test.getContainerAssignment(1, tw_start, tw_end);


    }
}
