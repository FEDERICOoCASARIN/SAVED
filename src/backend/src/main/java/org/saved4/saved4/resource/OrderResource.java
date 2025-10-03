package org.saved4.saved4.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.saved4.saved4.dto.Order;
import org.saved4.saved4.service.OrderService;

@RolesAllowed({"ADMIN", "COMPANY"})
@Path("/orders")
public class OrderResource {
    @Inject
    private OrderService orderService;

    // Get a specific order by ID
    @Path("/{orderId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Order getOrder(@PathParam("orderId") int orderId,
                          @QueryParam("order_for") String order_for) {
        Order order = orderService.getOrderById(orderId, order_for);
        if (order == null) {
            throw new WebApplicationException("Order not found", Response.Status.NOT_FOUND);
        }
        return order;
    }

    // List orders for a specific company/user
    @Path("/by-username/{username}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Order> listOrdersByUsername(@PathParam("username") String username) {
        return orderService.getFilteredOrders(null, null, username, null, null, null, null, null,
                                              null, null, null, null);
    }

    // List all orders with various query parameters
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Order> listOrders(@QueryParam("container_id") Integer containerId,
                                  @QueryParam("vehicle_id") Integer vehicleId,
                                  @QueryParam("requester") String requester,
                                  @QueryParam("start_date") String startDate,
                                  @QueryParam("end_date") String endDate,
                                  @QueryParam("source") String source,
                                  @QueryParam("destination") String destination,
                                  @QueryParam("status") List<String> statuses,
                                  @QueryParam("operation_type") String operationType,
                                  @QueryParam("order_for") String order_for,
                                  @QueryParam("is_shared") Boolean is_shared,
                                  @QueryParam("route_id") Integer routeId,
                                  @QueryParam("limit") @DefaultValue("100") int limit) {
        return orderService.getFilteredOrders(containerId, vehicleId, requester, startDate, endDate,
                                              source, destination, statuses, operationType,
                                              order_for, is_shared, routeId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public int createOrder(Order order) {
        Order createdOrder = orderService.addNewOrder(order);
        if (createdOrder != null) {
            return createdOrder.getOrderId();
        } else {
            throw new WebApplicationException("Failed to create order",
                                              Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Updates an existing order partially using a PATCH request.
     * Only the fields provided in the request body will be updated.
     *
     * @param orderId The unique identifier of the order to be updated.
     * @param order   The order object containing the fields to be updated, provided in JSON format.
     * @return The updated order object in JSON format.
     * @throws RuntimeException if the order with the given ID is not found.
     */
    @Path("/{orderId}")
    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Order updateOrder(@PathParam("orderId") int orderId, Order order) {
        Order currentOrder = orderService.getOrderById(orderId, null);
        if (currentOrder == null) {
            throw new WebApplicationException("Order not found with id " + orderId,
                                              Response.Status.NOT_FOUND);
        }

        if (order.getContainerId() != null) {
            currentOrder.setContainerId(order.getContainerId());
        }
        if (order.getVehicleId() != null) {
            currentOrder.setVehicleId(order.getVehicleId());
        }
        if (order.getRequester() != null) {
            currentOrder.setRequester(order.getRequester());
        }
        if (order.getRouteId() != null) {
            currentOrder.setRouteId(order.getRouteId());
        }
        if (order.getMadeAt() != null) {
            currentOrder.setMadeAt(order.getMadeAt());
        }
        if (order.getSource() != null) {
            currentOrder.setSource(order.getSource());
        }
        if (order.getDestination() != null) {
            currentOrder.setDestination(order.getDestination());
        }
        if (order.getShared() != null) {
            currentOrder.setShared(order.getShared());
        }
        if (order.getTwStart() != null) {
            currentOrder.setTwStart(order.getTwStart());
        }
        if (order.getTwEnd() != null) {
            currentOrder.setTwEnd(order.getTwEnd());
        }
        if (order.getEta() != null) {
            currentOrder.setEta(order.getEta());
        }
        if (order.getStatus() != null) {
            currentOrder.setStatus(order.getStatus());
        }
        if (order.getOperationType() != null) {
            currentOrder.setOperationType(order.getOperationType());
        }
        if (order.getFreightValue() != null) {
            currentOrder.setFreightValue(order.getFreightValue());
        }
        if (order.getFreightType() != null) {
            currentOrder.setFreightType(order.getFreightType());
        }
        if (order.getFreightWeight() != null) {
            currentOrder.setFreightWeight(order.getFreightWeight());
        }

        if (order.getOrder_for() != null) {
            currentOrder.setOrder_for(order.getOrder_for());
        }

        return orderService.updateOrder(orderId, currentOrder);
    }


    /**
     * Deletes an order by its ID.
     *
     * @param orderId The unique identifier of the order to be deleted.
     * @throws WebApplicationException if the order is not found (HTTP 404).
     */
    @Path("/{orderId}")
    @DELETE
    public void deleteOrder(@PathParam("orderId") int orderId) {
        boolean deleted = orderService.deleteOrder(orderId);
        if (!deleted) {
            throw new WebApplicationException("Order not found", Response.Status.NOT_FOUND);
        }
    }

    /**
     * Exports order data to an Excel (XLSX) file.
     *
     * @param order_for An optional query parameter to filter orders for export by their type.
     * @return A response containing the Excel file as a stream, with appropriate headers for download.
     * @throws WebApplicationException if an error occurs during Excel generation.
     */
    @GET
    @Path("/export/xlsx")
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public Response exportOrdersExcel(@QueryParam("order_for") String order_for) {
        List<Order> orders = orderService.exportOrders(order_for);

        StreamingOutput stream = output -> {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Orders Data");

                String[] headers =
                        {"Order ID", "Container ID", "Vehicle ID", "Requester", "Route ID",
                                "Made At", "Source", "Destination", "Is Shared", "TW Start",
                                "TW End", "ETA", "Status", "Operation Type", "Freight Value",
                                "Freight Type", "Freight Weight", "Order For"};

                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    Font headerFont = workbook.createFont();
                    headerFont.setBold(true);
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFont(headerFont);
                    cell.setCellStyle(headerCellStyle);
                }

                int rowNum = 1;
                for (Order order : orders) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(order.getOrderId());
                    row.createCell(1).setCellValue(
                            order.getContainerId() != null ? order.getContainerId().toString() :
                                    "");
                    row.createCell(2).setCellValue(
                            order.getVehicleId() != null ? order.getVehicleId().toString() : "");
                    row.createCell(3)
                            .setCellValue(order.getRequester() != null ? order.getRequester() : "");
                    row.createCell(4).setCellValue(
                            order.getRouteId() != null ? order.getRouteId().toString() : "");
                    row.createCell(5).setCellValue(
                            order.getMadeAt() != null ? order.getMadeAt().toString() : "");
                    row.createCell(6)
                            .setCellValue(order.getSource() != null ? order.getSource() : "");
                    row.createCell(7).setCellValue(
                            order.getDestination() != null ? order.getDestination() : "");
                    row.createCell(8).setCellValue(
                            order.getShared() != null ? order.getShared().toString() : "");
                    row.createCell(9).setCellValue(
                            order.getTwStart() != null ? order.getTwStart().toString() : "");
                    row.createCell(10).setCellValue(
                            order.getTwEnd() != null ? order.getTwEnd().toString() : "");
                    row.createCell(11)
                            .setCellValue(order.getEta() != null ? order.getEta().toString() : "");
                    row.createCell(12).setCellValue(
                            order.getStatus() != null ? order.getStatus().toString() : "");
                    row.createCell(13).setCellValue(
                            order.getOperationType() != null ? order.getOperationType().toString() :
                                    "");
                    row.createCell(14).setCellValue(
                            order.getFreightValue() != null ? order.getFreightValue().toString() :
                                    "");
                    row.createCell(15).setCellValue(
                            order.getFreightType() != null ? order.getFreightType().toString() :
                                    "");
                    row.createCell(16).setCellValue(
                            order.getFreightWeight() != null ? order.getFreightWeight().toString() :
                                    "");
                    row.createCell(17)
                            .setCellValue(order.getOrder_for() != null ? order.getOrder_for() : "");
                }

                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                workbook.write(output);
                output.flush();
            } catch (IOException e) {
                System.err.println("Error while writing excel: " + e.getMessage());
                throw new WebApplicationException("Excel not generated: " + e.getMessage(), e);
            }
        };

        return Response.ok(stream)
                .header("Content-Disposition", "attachment; filename=\"orders_export.xlsx\"")
                .build();
    }

    /**
     * Exports order data to a Comma Separated Values (CSV) file.
     *
     * @param order_for An optional query parameter to filter orders for export by their type.
     * @return A response containing the CSV file as a stream, with appropriate headers for download.
     * @throws WebApplicationException if an error occurs during CSV generation.
     */
    @GET
    @Path("/export/csv")
    @Produces("text/csv")
    public Response exportOrdersCSV(@QueryParam("order_for") String order_for) {
        List<Order> orders = orderService.exportOrders(order_for);

        StreamingOutput stream = output -> {
            try {
                String[] headers =
                        {"Order ID", "Container ID", "Vehicle ID", "Requested by", "Route ID",
                                "Made At", "Source", "Destination", "Is Shared", "TW Start",
                                "TW End", "ETA", "Status", "Operation Type", "Freight Value",
                                "Freight Type", "Freight Weight"};
                StringBuilder csvBuilder = new StringBuilder();

                for (String header : headers) {
                    csvBuilder.append(header).append(",");
                }
                csvBuilder.setLength(csvBuilder.length() - 1);
                csvBuilder.append("\n");

                for (Order order : orders) {
                    csvBuilder.append(order.getOrderId()).append(",");
                    csvBuilder.append(order.getContainerId() != null ? order.getContainerId() : "")
                            .append(",");
                    csvBuilder.append(order.getVehicleId() != null ? order.getVehicleId() : "")
                            .append(",");
                    csvBuilder.append(order.getRequester() != null ? order.getRequester() : "")
                            .append(",");
                    csvBuilder.append(order.getRouteId() != null ? order.getRouteId() : "")
                            .append(",");
                    csvBuilder.append(order.getMadeAt() != null ? order.getMadeAt().toString() : "")
                            .append(",");
                    csvBuilder.append(order.getSource() != null ? order.getSource() : "")
                            .append(",");
                    csvBuilder.append(order.getDestination() != null ? order.getDestination() : "")
                            .append(",");
                    csvBuilder.append(order.getShared() != null ? order.getShared().toString() : "")
                            .append(",");
                    csvBuilder.append(
                                    order.getTwStart() != null ? order.getTwStart().toString() : "")
                            .append(",");
                    csvBuilder.append(order.getTwEnd() != null ? order.getTwEnd().toString() : "")
                            .append(",");
                    csvBuilder.append(order.getEta() != null ? order.getEta().toString() : "")
                            .append(",");
                    csvBuilder.append(order.getStatus() != null ? order.getStatus().toString() : "")
                            .append(",");
                    csvBuilder.append(
                                    order.getOperationType() != null ? order.getOperationType() : "")
                            .append(",");
                    csvBuilder.append(
                            order.getFreightValue() != null ? order.getFreightValue().toString() :
                                    "").append(",");
                    csvBuilder.append(order.getFreightType() != null ? order.getFreightType() : "")
                            .append(",");
                    csvBuilder.append(
                            order.getFreightWeight() != null ? order.getFreightWeight().toString() :
                                    "").append("\n");
                }

                output.write(csvBuilder.toString().getBytes());
                output.flush();
            } catch (IOException e) {
                System.err.println("Error while writing CSV: " + e.getMessage());
                throw new WebApplicationException("CSV not generated: " + e.getMessage(), e);
            }
        };

        return Response.ok(stream)
                .header("Content-Disposition", "attachment; filename=\"orders_export.csv\"")
                .build();
    }

    /**
     * Activate an order.
     */
    @GET
    @Path("/activate")
    @Produces(MediaType.APPLICATION_JSON)
    public void activateOrder() {
        boolean deleted = orderService.activateOrder();
        if (!deleted) {
            throw new WebApplicationException("Order not found", Response.Status.NOT_FOUND);
        }
    }

    /**
     * Cancels all orders for a specific user that haven't started yet.
     * This is typically called when deleting a user to clean up their pending orders.
     *
     * @param username The username of the user whose orders should be canceled
     * @return Response indicating the result of the operation
     */
    @Path("/cancel-user-orders/{username}")
    @PATCH
    @Produces(MediaType.APPLICATION_JSON)
    public Response cancelUserOrders(@PathParam("username") String username) {
        try {
            int canceledCount = orderService.cancelOrdersForUser(username);
            return Response.ok()
                    .entity("{\"message\": \"" + canceledCount + " orders canceled for user " +
                                    username + "\"}").build();
        } catch (Exception e) {
            System.err.println(
                    "Error canceling orders for user " + username + ": " + e.getMessage());
            e.printStackTrace();
            throw new WebApplicationException("Failed to cancel orders for user: " + e.getMessage(),
                                              Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}
