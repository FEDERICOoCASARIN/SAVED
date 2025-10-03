package org.saved4.saved4.dao;

import jakarta.inject.Singleton;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.saved4.saved4.DBProvider;
import org.saved4.saved4.dto.Coordinate;
import org.saved4.saved4.dto.Order;
import org.saved4.saved4.enums.FreightType;
import org.saved4.saved4.enums.OperationType;
import org.saved4.saved4.enums.OrderStatus;
import org.saved4.saved4.enums.Role;


@Singleton
public class OrderDao {

    /**
     * Retrieves a list of all orders from the database.
     * Orders are ordered by their date of creation in descending order.
     *
     * @return A list of order objects representing all orders found in the database.
     * Returns an empty list if no orders are found or if a SQLException occurs.
     */
    public List<Order> getAllOrdersDB(Role role) {
        List<Order> orders = new ArrayList<>();
        String sql = """
                SELECT o.*,
                   COALESCE(sc.location, sp.location) AS source_location,
                   COALESCE(dc.location, dp.location) AS destination_location
                FROM orders o
                JOIN users us ON o.source = us.username
                LEFT JOIN companies sc ON o.source = sc.username
                LEFT JOIN port sp ON o.source = sp.username
                JOIN users ud ON o.destination = ud.username
                LEFT JOIN companies dc ON o.destination = dc.username
                LEFT JOIN port dp ON o.destination = dp.username
                ORDER BY made_at DESC
                
                """;


        try (Connection conn = DBProvider.getConnection(role);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                int containerId = rs.getInt("container_id");
                if (rs.wasNull()) {
                    order.setContainerId(null);
                } else {
                    order.setContainerId(containerId);
                }

                int vehicleId = rs.getInt("vehicle_id");
                if (rs.wasNull()) {
                    order.setVehicleId(null);
                } else {
                    order.setVehicleId(vehicleId);
                }

                order.setRequester(rs.getString("requester"));
                order.setRouteId(rs.getInt("route_id"));
                order.setMadeAt(rs.getTimestamp("made_at"));
                order.setSource(rs.getString("source"));
                order.setOrder_for(rs.getString("order_for"));
                //  source location
                String source_loc = rs.getString("source_location");
                if (source_loc != null) {
                    source_loc = source_loc.replace("(", "").replace(")", "").trim();
                    String[] coords = source_loc.split(",");
                    double x = Double.parseDouble(coords[0].trim());
                    double y = Double.parseDouble(coords[1].trim());
                    order.setSourcePosition(new Coordinate(x, y));
                }

                order.setDestination(rs.getString("destination"));

                // destination location
                String dest_loc = rs.getString("destination_location");
                if (dest_loc != null) {
                    dest_loc = dest_loc.replace("(", "").replace(")", "").trim();
                    String[] coords = dest_loc.split(",");
                    double x = Double.parseDouble(coords[0].trim());
                    double y = Double.parseDouble(coords[1].trim());
                    order.setDestinationPosition(new Coordinate(x, y));
                }

                String freightTypeStr = rs.getString("freight_type");
                if (freightTypeStr != null) {
                    order.setFreightType(FreightType.valueOf(freightTypeStr));
                } else {
                    order.setFreightType(null);
                }

                order.setFreightWeight(rs.getBigDecimal("freight_weight"));

                order.setShared(rs.getBoolean("is_shared"));
                order.setPreferredShared(rs.getBoolean("preferred_shared"));
                order.setTwStart(rs.getTimestamp("tw_start"));
                order.setTwEnd(rs.getTimestamp("tw_end"));
                order.setEta(rs.getTimestamp("eta"));

                order.setStatus(OrderStatus.valueOf(rs.getString("status")));
                order.setOperationType(OperationType.valueOf(rs.getString("operation_type")));
                order.setFreightValue(rs.getBigDecimal("freight_value"));

                order.setDepartureTime(rs.getTimestamp("departure_time"));
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving orders: " + e.getMessage());
        }

        return orders;

    }


    /**
     * Retrieves a specific order from the database by its unique identifier.
     * An optional orderFor parameter can be provided to further filter the search.
     *
     * @param orderId  The unique integer ID of the order to retrieve.
     * @param orderFor An optional string to filter orders by their 'order_for' recipient. Can be null.
     * @return The order object if found, or null if no matching order is found or a SQLException occurs.
     */
    public Order getOrderByIdDB(Role role, String dbRequester, int orderId, String orderFor) {
        StringBuilder sqlBuilder = new StringBuilder("""
                                                             SELECT o.*,
                                                                    COALESCE(sc.location, sp.location) AS source_location,
                                                                    COALESCE(dc.location, dp.location) AS destination_location
                                                             FROM orders o
                                                             JOIN users us ON o.source = us.username
                                                             LEFT JOIN companies sc ON o.source = sc.username
                                                             LEFT JOIN port sp ON o.source = sp.username
                                                             JOIN users ud ON o.destination = ud.username
                                                             LEFT JOIN companies dc ON o.destination = dc.username
                                                             LEFT JOIN port dp ON o.destination = dp.username
                                                             WHERE o.order_id = ?
                                                             """);


        if (orderFor != null) {
            sqlBuilder.append(" AND o.order_for = ?");
        }

        Order order = null;
        try (Connection connection = DBProvider.getConnection(role, dbRequester);
             PreparedStatement preparedStatement = connection.prepareStatement(
                     sqlBuilder.toString())) {

            int paramIndex = 1;
            preparedStatement.setInt(paramIndex++, orderId);

            if (orderFor != null) {
                preparedStatement.setString(paramIndex++, orderFor);
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    order = new Order();
                    order.setOrderId(resultSet.getInt("order_id"));
                    order.setContainerId(resultSet.getInt("container_id"));
                    // Check for null after getting the int, using wasNull()
                    if (resultSet.wasNull()) {
                        order.setContainerId(null);
                    }
                    order.setVehicleId(resultSet.getInt("vehicle_id"));
                    // Check for null after getting the int, using wasNull()
                    if (resultSet.wasNull()) {
                        order.setVehicleId(null);
                    }
                    order.setRequester(resultSet.getString("requester"));
                    order.setOrder_for(resultSet.getString("order_for"));

                    order.setRouteId(resultSet.getInt("route_id"));
                    // Check for null after getting the int, using wasNull()
                    if (resultSet.wasNull()) {
                        order.setRouteId(null);
                    }
                    //                    order.setRouteId(resultSet.getInt("route_id"));
                    order.setMadeAt(resultSet.getTimestamp("made_at"));
                    order.setSource(resultSet.getString("source"));
                    order.setDestination(resultSet.getString("destination"));
                    order.setShared(resultSet.getBoolean("is_shared"));
                    order.setShared(resultSet.getBoolean("preferred_shared"));
                    order.setTwStart(resultSet.getTimestamp("tw_start"));
                    order.setTwEnd(resultSet.getTimestamp("tw_end"));

                    order.setEta(resultSet.getTimestamp("eta"));
                    order.setStatus(OrderStatus.valueOf(resultSet.getString("status")));
                    order.setOperationType(
                            OperationType.valueOf(resultSet.getString("operation_type")));
                    order.setFreightValue(resultSet.getBigDecimal("freight_value"));

                    String freightTypeStr = resultSet.getString("freight_type");
                    if (resultSet.wasNull() || freightTypeStr == null) {
                        order.setFreightType(null);
                    } else {
                        order.setFreightType(FreightType.valueOf(freightTypeStr));
                    }
                    order.setFreightWeight(resultSet.getBigDecimal("freight_weight"));
                    order.setDepartureTime(resultSet.getTimestamp("departure_time"));


                    // Parse source location
                    String sourcePos = resultSet.getString("source_location");
                    if (sourcePos != null) {
                        sourcePos = sourcePos.replace("(", "").replace(")", "").trim();
                        String[] coords = sourcePos.split(",");
                        double x = Double.parseDouble(coords[0].trim());
                        double y = Double.parseDouble(coords[1].trim());
                        order.setSourcePosition(new Coordinate(x, y));
                    }

                    // Parse destination location
                    String destPos = resultSet.getString("destination_location");
                    if (destPos != null) {
                        destPos = destPos.replace("(", "").replace(")", "").trim();
                        String[] destCoords = destPos.split(",");
                        double x = Double.parseDouble(destCoords[0].trim());
                        double y = Double.parseDouble(destCoords[1].trim());
                        order.setDestinationPosition(new Coordinate(x, y));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving order: " + e.getMessage());
            e.printStackTrace();
        }

        return order;
    }


    /**
     * Inserts a new order into the database.
     * The order's `order_id` will be set by the database and returned in the order object.
     * The `is_shared` field is hardcoded to `false` upon insertion, since it's a default value.
     *
     * @param order The order object containing the data to be inserted.
     * @return The order object with its newly generated order_id, null if the insertion fails.
     */
    public Order insertOrderDB(Role role, String dbRequester, Order order) {
        String sql =
                "INSERT INTO orders (container_id, vehicle_id, requester, route_id, made_at, source, destination, " +
                        "preferred_shared, tw_start, tw_end, eta, status, operation_type, freight_value, freight_type, freight_weight, order_for, is_shared) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, false) RETURNING order_id";

        try (Connection conn = DBProvider.getConnection(role, dbRequester);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(true);
            setOrderParameters(stmt, order);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    order.setOrderId(rs.getInt("order_id"));
                    System.out.println("added");
                    return order;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting order: " + e.getMessage());
        }
        return null;
    }

    /**
     * Updates an existing order in the database.
     *
     * @param id    The unique integer ID of the order to update.
     * @param order The order object containing the updated values. Only non-null fields
     *              in this object will be used to update the corresponding columns in the database.
     * @return The updated order object if the update is successful, or null if
     * no order is found with the given ID or a SQLException occurs.
     */
    public Order updateOrderDB(Role role, String dbRequester, int id, Order order) {
        StringBuilder sql = new StringBuilder("UPDATE orders SET ");
        List<Object> params = new ArrayList<>();

        sql.append("container_id = ?, ");
        if (order.getContainerId() == null || order.getContainerId() == 0) {
            System.out.println("container id null");
            params.add(null);
        } else {
            System.out.println("container id not null");
            params.add(order.getContainerId());
        }

        if (order.getVehicleId() != null) {
            sql.append("vehicle_id = ?, ");
            params.add(order.getVehicleId());
        }
        if (order.getRequester() != null) {
            sql.append("requester = ?, ");
            params.add(order.getRequester());
        }
        if (order.getRouteId() != null) {
            sql.append("route_id = ?, ");
            params.add(order.getRouteId());
        }
        if (order.getMadeAt() != null) {
            sql.append("made_at = ?, ");
            params.add(Timestamp.valueOf(order.getMadeAt().toLocalDateTime()));
        }
        if (order.getSource() != null) {
            sql.append("source = ?, ");
            params.add(order.getSource());
        }
        if (order.getDestination() != null) {
            sql.append("destination = ?, ");
            params.add(order.getDestination());
        }
        if (order.getShared() != null) {
            sql.append("is_shared = ?, ");
            params.add(order.getShared());
        }

        if (order.getPreferredShared() != null) {
            sql.append("preferred_shared = ?, ");
            params.add(order.getPreferredShared());
        }
        if (order.getTwStart() != null) {
            sql.append("tw_start = ?, ");
            params.add(Timestamp.valueOf(order.getTwStart().toLocalDateTime()));
        }
        if (order.getTwEnd() != null) {
            sql.append("tw_end = ?, ");
            params.add(Timestamp.valueOf(order.getTwEnd().toLocalDateTime()));
        }
        if (order.getEta() != null) {
            sql.append("eta = ?, ");
            params.add(Timestamp.valueOf(order.getEta().toLocalDateTime()));
        }
        if (order.getStatus() != null) {
            sql.append("status = ?::order_status, ");
            params.add(order.getStatus().getDbValue());
        }
        if (order.getOperationType() != null) {
            sql.append("operation_type = ?::operation_type, ");
            params.add(order.getOperationType().getDbValue());
        }
        if (order.getFreightValue() != null) {
            sql.append("freight_value = ?, ");
            params.add(order.getFreightValue());
        }
        if (order.getFreightType() != null) {
            sql.append("freight_type = ?::freight_type, ");
            params.add(order.getFreightType().getDbValue());
        }
        if (order.getFreightWeight() != null) {
            sql.append("freight_weight = ?, ");
            params.add(order.getFreightWeight());
        }

        if (order.getDepartureTime() != null) {
            sql.append("departure_time = ?, ");
            params.add(Timestamp.valueOf(order.getDepartureTime().toLocalDateTime()));
        }

        if (order.getOrder_for() != null) {
            sql.append("order_for = ?, ");
            params.add(order.getOrder_for());
        }


        // No fields to update
        if (params.isEmpty()) {
            System.out.println("No fields to update.");
            return order;
        }

        // Remove last comma and space
        sql.setLength(sql.length() - 2);

        // Add WHERE clause
        sql.append(" WHERE order_id = ?");
        params.add(id);

        try (Connection conn = DBProvider.getConnection(role, dbRequester);
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            conn.setAutoCommit(true);

            // Set parameters one by one
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof Timestamp) {
                    stmt.setTimestamp(i + 1, (Timestamp) param);
                } else if (param instanceof Integer) {
                    stmt.setInt(i + 1, (Integer) param);
                } else if (param instanceof Boolean) {
                    stmt.setBoolean(i + 1, (Boolean) param);
                } else if (param instanceof BigDecimal) {
                    stmt.setBigDecimal(i + 1, (BigDecimal) param);
                } else {
                    stmt.setObject(i + 1, param);
                }
            }

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                order.setOrderId(id);
                System.out.println("Success updating order " + id);
                return order;
            }

        } catch (SQLException e) {
            System.err.println("Error updating order " + id + ": " + e.getMessage());
        }

        return null;
    }


    /**
     * Deletes an order from the database by its unique identifier.
     *
     * @param id The unique integer ID of the order to be removed.
     * @return true if the order was successfully deleted, false otherwise.
     */
    public boolean removeOrderDB(Role role, String dbRequester, int id) {
        String sql = "DELETE FROM orders WHERE order_id = ?";
        try (Connection conn = DBProvider.getConnection(role, dbRequester);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            conn.setAutoCommit(true);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Success REMOVE with id: " + id);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting order " + id + ": " + e.getMessage());
            return false;
        }
        return false;
    }

    /**
     * Retrieve filtered orders based on query parameters.
     */
    public List<Order> getFilteredOrders(Role role, String dbRequester, Integer containerId,
                                         Integer vehicleId, String requester, String startDate,
                                         String endDate, String source, String destination,
                                         List<String> status, String operationType,
                                         String order_for, Boolean is_shared, Integer routeId) {
        StringBuilder sql = new StringBuilder("""
                                                      SELECT o.*,
                                                             COALESCE(sc.location, sp.location) AS source_location,
                                                             COALESCE(dc.location, dp.location) AS destination_location
                                                      FROM orders o
                                                      JOIN users us ON o.source = us.username
                                                      LEFT JOIN companies sc ON o.source = sc.username
                                                      LEFT JOIN port sp ON o.source = sp.username
                                                      JOIN users ud ON o.destination = ud.username
                                                      LEFT JOIN companies dc ON o.destination = dc.username
                                                      LEFT JOIN port dp ON o.destination = dp.username
                                                      """);

        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (containerId != null) {
            conditions.add("o.container_id = ?");
            params.add(containerId);
        }

        if (vehicleId != null) {
            conditions.add("o.vehicle_id = ?");
            params.add(vehicleId);
        }

        if (requester != null && !requester.isEmpty()) {
            conditions.add("o.requester = ?");
            params.add(requester);
        }
        if (startDate != null && !startDate.isEmpty()) {
            conditions.add("o.made_at >= ?");
            params.add(Timestamp.valueOf(startDate));
        }
        if (endDate != null && !endDate.isEmpty()) {
            conditions.add("o.made_at <= ?");
            params.add(Timestamp.valueOf(endDate));
        }
        if (source != null && !source.isEmpty()) {
            conditions.add("o.source = ?");
            params.add(source);
        }
        if (destination != null && !destination.isEmpty()) {
            conditions.add("o.destination = ?");
            params.add(destination);
        }
        if (is_shared != null) {
            conditions.add("o.is_shared = ?");
            params.add(is_shared);
        }

        if (status != null && !status.isEmpty()) {

            String placeholders =
                    status.stream().map(s -> "?::order_status").collect(Collectors.joining(", "));
            conditions.add("o.status IN (" + placeholders + ")");
            params.addAll(status);
        }

        if (operationType != null && !operationType.isEmpty()) {
            conditions.add("o.operation_type = ?::operation_type");
            params.add(operationType);
        }


        if (routeId != null) {
            conditions.add("o.route_id = ?");
            params.add(routeId);
        }

        if (order_for != null) {
            conditions.add("o.order_for = ?");
            params.add(order_for);
        }

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(String.join(" AND ", conditions));
        }

        sql.append(" ORDER BY order_id DESC");

        try (Connection conn = DBProvider.getConnection(role, dbRequester);
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                List<Order> orders = new ArrayList<>();
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
                return orders;
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving filtered orders: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    /**
     * Maps a result row to an order object.
     *
     * @param resultSet The resultSet from which to extract order data.
     * @return An order object populated with data from the current row of the ResultSet.
     * @throws SQLException If a database access error occurs or this method is called on a closed result set.
     */
    private Order mapResultSetToOrder(ResultSet resultSet) throws SQLException {
        Order order = new Order();

        order.setOrderId(resultSet.getInt("order_id"));
        int containerId = resultSet.getInt("container_id");
        if (resultSet.wasNull()) {
            order.setContainerId(null); // assuming order.setContainerId takes Integer
        } else {
            order.setContainerId(containerId);
        }

        int vehicleId = resultSet.getInt("vehicle_id");
        if (resultSet.wasNull()) {
            order.setVehicleId(null); // assuming order.setContainerId takes Integer
        } else {
            order.setVehicleId(vehicleId);
        }


        order.setRequester(resultSet.getString("requester"));

        int routeId = resultSet.getInt("route_id");
        if (resultSet.wasNull()) {
            order.setRouteId(null);
        } else {
            order.setRouteId(routeId);
        }
        //        order.setRouteId(resultSet.getInt("route_id"));
        order.setMadeAt(resultSet.getTimestamp("made_at"));
        order.setSource(resultSet.getString("source"));
        order.setDestination(resultSet.getString("destination"));
        order.setShared(resultSet.getBoolean("is_shared"));
        order.setPreferredShared(resultSet.getBoolean("preferred_shared"));
        order.setTwStart(resultSet.getTimestamp("tw_start"));
        order.setTwEnd(resultSet.getTimestamp("tw_end"));
        order.setEta(resultSet.getTimestamp("eta"));
        order.setStatus(OrderStatus.valueOf(resultSet.getString("status")));
        order.setOperationType(OperationType.valueOf(resultSet.getString("operation_type")));
        order.setFreightValue(resultSet.getBigDecimal("freight_value"));
        order.setOrder_for(resultSet.getString("order_for"));

        String freightTypeStr = resultSet.getString("freight_type");
        if (freightTypeStr != null) {
            order.setFreightType(FreightType.valueOf(freightTypeStr));
        } else {
            order.setFreightType(null);
        }
        order.setFreightWeight(resultSet.getBigDecimal("freight_weight"));
        order.setDepartureTime(resultSet.getTimestamp("departure_time"));


        // Parse source location
        String sourcePos = resultSet.getString("source_location");
        if (sourcePos != null) {
            sourcePos = sourcePos.replace("(", "").replace(")", "").trim();
            String[] coords = sourcePos.split(",");
            double x = Double.parseDouble(coords[0].trim());
            double y = Double.parseDouble(coords[1].trim());
            order.setSourcePosition(new Coordinate(x, y));
        }

        // Parse destination location
        String destPos = resultSet.getString("destination_location");
        if (destPos != null) {
            destPos = destPos.replace("(", "").replace(")", "").trim();
            String[] destCoords = destPos.split(",");
            double x = Double.parseDouble(destCoords[0].trim());
            double y = Double.parseDouble(destCoords[1].trim());
            order.setDestinationPosition(new Coordinate(x, y));
        }

        return order;
    }


    /**
     * Set PreparedStatement parameters from Order object
     */
    private void setOrderParameters(PreparedStatement stmt, Order order) throws SQLException {
        if (order.getContainerId() != null) {
            stmt.setInt(1, order.getContainerId());
        } else {
            stmt.setObject(1, null);
        }

        if (order.getVehicleId() != null) {
            stmt.setInt(2, order.getVehicleId());
        } else {
            stmt.setNull(2, Types.INTEGER);
        }


        stmt.setString(3, order.getRequester());

        if (order.getRouteId() != null) {
            stmt.setInt(4, order.getRouteId());
        } else {
            stmt.setNull(4, Types.INTEGER);
        }
        stmt.setTimestamp(5, Timestamp.valueOf(order.getMadeAt().toLocalDateTime()));
        stmt.setString(6, order.getSource());
        stmt.setString(7, order.getDestination());
        stmt.setBoolean(8, order.getPreferredShared());

        stmt.setTimestamp(9, Timestamp.valueOf(order.getTwStart().toLocalDateTime()));
        stmt.setTimestamp(10, Timestamp.valueOf(order.getTwEnd().toLocalDateTime()));

        if (order.getEta() != null) {
            stmt.setTimestamp(11, Timestamp.valueOf(order.getEta().toLocalDateTime()));
        } else {
            stmt.setNull(11, Types.TIMESTAMP);
        }

        stmt.setObject(12, order.getStatus().getDbValue(), Types.OTHER);
        stmt.setObject(13, order.getOperationType().getDbValue(), Types.OTHER);

        if (order.getFreightValue() != null) {
            stmt.setBigDecimal(14, order.getFreightValue());
        } else {
            stmt.setBigDecimal(14, null);
        }
        if (order.getFreightType() != null) {
            stmt.setObject(15, order.getFreightType().getDbValue(), Types.OTHER);
        } else {
            stmt.setObject(15, null, Types.OTHER);
        }
        if (order.getFreightWeight() != null) {
            stmt.setBigDecimal(16, order.getFreightWeight());
        } else {
            stmt.setBigDecimal(16, null);
        }

        stmt.setString(17, order.getOrder_for());

    }


    /**
     * Inserting new route.
     *
     * @return integer of the new route
     */
    public Integer insertRouteDB(Role role) {
        String sql = "INSERT INTO routes (route) " + "VALUES (?) RETURNING route_id";

        try (Connection conn = DBProvider.getConnection(role);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(true);
            stmt.setObject(1, null);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("added");
                    return rs.getInt("route_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting order: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves orders for a specific user filtered by status.
     * Used for canceling orders when deleting a user.
     *
     * @param username The username to filter orders by
     * @param statuses List of order statuses to include
     * @return List of orders matching the criteria
     */
    public List<Order> getOrdersForUserByStatus(String username, List<OrderStatus> statuses) {
        List<Order> orders = new ArrayList<>();

        // Convert statuses to string list for SQL IN clause
        String statusPlaceholders =
                statuses.stream().map(status -> "?").collect(Collectors.joining(","));

        String sql = "SELECT o.*, " + "COALESCE(sc.location, sp.location) AS source_location, " +
                "COALESCE(dc.location, dp.location) AS destination_location " + "FROM orders o " +
                "JOIN users us ON o.source = us.username " +
                "LEFT JOIN companies sc ON o.source = sc.username " +
                "LEFT JOIN port sp ON o.source = sp.username " +
                "JOIN users ud ON o.destination = ud.username " +
                "LEFT JOIN companies dc ON o.destination = dc.username " +
                "LEFT JOIN port dp ON o.destination = dp.username " +
                "WHERE (o.requester = ? OR o.order_for = ?) " + "AND o.status IN (" +
                statusPlaceholders + ") " + "ORDER BY made_at DESC";

        try (Connection conn = DBProvider.getConnection(Role.ADMIN);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            stmt.setString(paramIndex++, username);
            stmt.setString(paramIndex++, username);

            for (OrderStatus status : statuses) {
                stmt.setString(paramIndex++, status.name());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("order_id"));
                    order.setContainerId(rs.getObject("container_id", Integer.class));
                    order.setVehicleId(rs.getObject("vehicle_id", Integer.class));
                    order.setRequester(rs.getString("requester"));
                    order.setRouteId(rs.getObject("route_id", Integer.class));

                    Timestamp madeAtTimestamp = rs.getTimestamp("made_at");
                    if (madeAtTimestamp != null) {
                        order.setMadeAt(madeAtTimestamp);
                    }

                    order.setSource(rs.getString("source"));
                    order.setDestination(rs.getString("destination"));
                    order.setShared(rs.getBoolean("shared"));

                    Timestamp twStartTimestamp = rs.getTimestamp("tw_start");
                    if (twStartTimestamp != null) {
                        order.setTwStart(twStartTimestamp);
                    }

                    Timestamp twEndTimestamp = rs.getTimestamp("tw_end");
                    if (twEndTimestamp != null) {
                        order.setTwEnd(twEndTimestamp);
                    }

                    Timestamp etaTimestamp = rs.getTimestamp("eta");
                    if (etaTimestamp != null) {
                        order.setEta(etaTimestamp);
                    }

                    String statusStr = rs.getString("status");
                    if (statusStr != null) {
                        order.setStatus(OrderStatus.valueOf(statusStr));
                    }

                    String operationTypeStr = rs.getString("operation_type");
                    if (operationTypeStr != null) {
                        order.setOperationType(OperationType.valueOf(operationTypeStr));
                    }

                    BigDecimal freightValue = rs.getBigDecimal("freight_value");
                    if (freightValue != null) {
                        order.setFreightValue(freightValue);
                    }

                    String freightTypeStr = rs.getString("freight_type");
                    if (freightTypeStr != null) {
                        order.setFreightType(FreightType.valueOf(freightTypeStr));
                    }

                    BigDecimal freightWeight = rs.getBigDecimal("freight_weight");
                    if (freightWeight != null) {
                        order.setFreightWeight(freightWeight);
                    }

                    order.setOrder_for(rs.getString("order_for"));

                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            System.err.println(
                    "Error retrieving orders for user " + username + " with statuses " + statuses +
                            ": " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }
}
