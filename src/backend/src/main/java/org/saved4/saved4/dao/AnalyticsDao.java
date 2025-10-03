package org.saved4.saved4.dao;

import jakarta.inject.Singleton;
import org.saved4.saved4.DBProvider;
import org.saved4.saved4.enums.FreightType;
import org.saved4.saved4.enums.Role;
import org.saved4.saved4.enums.VehicleStatus;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Data Access Object (DAO) for retrieving analytics data from the database.
 * This class handles all database interactions related to analytics, providing methods
 * to fetch various statistics about orders, vehicles, and shipments.
 */
@Singleton
public class AnalyticsDao {

    /**
     * Appends common WHERE clauses for filtering by company name and date range to a SQL query.
     * This method is used by multiple data retrieval methods to ensure consistent filtering logic.
     *
     * @param sqlBuilder       The StringBuilder containing the base SQL query.
     * @param params           The list of parameters to be used in the PreparedStatement.
     * @param companyName      The name of the company to filter by. Can be null or empty for no company filter.
     * @param startDateStr     The start date string (e.g., "YYYY-MM-DD") for filtering. Can be null or empty.
     * @param endDateStr       The end date string (e.g., "YYYY-MM-DD") for filtering. Can be null or empty.
     * @param dateColumn       The name of the date column in the database table to apply the filter on (e.g., "o.made_at").
     * @param hasExistingWhere True if the sqlBuilder already contains a WHERE clause (e.g., "WHERE 1=1" or "WHERE status='completed'"), false otherwise.
     *                         This helps in correctly appending "AND" or "WHERE".
     */
    private void appendCommonFilters(StringBuilder sqlBuilder, List<Object> params,
                                     String companyName, String startDateStr, String endDateStr,
                                     String dateColumn, boolean hasExistingWhere) {
        String conjunction = hasExistingWhere ? " AND " : " WHERE ";

        if (companyName != null && !companyName.trim().isEmpty()) {
            sqlBuilder.append(conjunction).append("o.order_for = ?");
            params.add(companyName);
            conjunction = " AND "; // Subsequent conditions will also use AND
        }

        boolean hasStartDate = startDateStr != null && !startDateStr.trim().isEmpty();
        boolean hasEndDate = endDateStr != null && !endDateStr.trim().isEmpty();

        if (hasStartDate && hasEndDate) {
            sqlBuilder.append(conjunction).append(dateColumn).append(" BETWEEN ? AND ?");
            LocalDate startDate = LocalDate.parse(startDateStr);
            params.add(Timestamp.valueOf(LocalDateTime.of(startDate, LocalTime.MIN)));
            LocalDate endDate = LocalDate.parse(endDateStr);
            params.add(Timestamp.valueOf(LocalDateTime.of(endDate, LocalTime.MAX)));
        } else if (hasStartDate) {
            sqlBuilder.append(conjunction).append(dateColumn).append(" >= ?");
            LocalDate startDate = LocalDate.parse(startDateStr);
            params.add(Timestamp.valueOf(LocalDateTime.of(startDate, LocalTime.MIN)));
        } else if (hasEndDate) {
            sqlBuilder.append(conjunction).append(dateColumn).append(" <= ?");
            LocalDate endDate = LocalDate.parse(endDateStr);
            params.add(Timestamp.valueOf(LocalDateTime.of(endDate, LocalTime.MAX)));
        }
    }

    /**
     * Sets parameters for a PreparedStatement based on their type.
     *
     * @param ps     The PreparedStatement to set parameters for.
     * @param params The list of parameters (String, Timestamp, Integer, Double) to set.
     * @throws SQLException If a database access error occurs.
     */
    private void setParameters(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object param = params.get(i);
            if (param instanceof String) {
                ps.setString(i + 1, (String) param);
            } else if (param instanceof Timestamp) {
                ps.setTimestamp(i + 1, (Timestamp) param);
            } else if (param instanceof Integer) {
                ps.setInt(i + 1, (Integer) param);
            } else if (param instanceof Double) {
                ps.setDouble(i + 1, (Double) param);
            }
        }
    }

    /**
     * Retrieves the total count of all vehicles.
     *
     * @return The total number of vehicles.
     */
    public int getTotalVehicleCount() {
        String sql = "SELECT COUNT(*) FROM vehicles";
        try (Connection conn = DBProvider.getConnection(Role.SYSTEM); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching total vehicles: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retrieves the count of vehicles that are currently marked as 'available'.
     *
     * @return The number of available vehicles.
     */
    public int getAvailableVehicles() {
        String sql = """
                SELECT COUNT(vehicle_id)
                FROM vehicles
                WHERE status = ?::vehicle_status
                """;
        try (Connection conn = DBProvider.getConnection(Role.SYSTEM);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, VehicleStatus.available.name());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching available vehicle count: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Retrieves the distribution of vehicles by their status (e.g., available, in_use, out_of_order).
     *
     * @return A Map where keys are VehicleStatus enums and values are their respective counts.
     */
    public Map<VehicleStatus, Integer> getVehicleStatusDistribution() {
        Map<VehicleStatus, Integer> result = new HashMap<>();
        String sql = "SELECT status, COUNT(*) AS count FROM vehicles GROUP BY status";
        try (Connection conn = DBProvider.getConnection(Role.SYSTEM); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String statusStr = rs.getString("status");
                try {
                    VehicleStatus status = VehicleStatus.valueOf(statusStr);
                    result.put(status, rs.getInt("count"));
                } catch (IllegalArgumentException e) {
                    System.err.println("Unknown vehicle status: " + statusStr);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching vehicle status distribution: " + e.getMessage());
        }

        // Ensure all enum statuses are present, even if count is 0
        for (VehicleStatus status : VehicleStatus.values()) {
            result.putIfAbsent(status, 0);
        }
        return result;
    }

    /**
     * Retrieves the total number of orders, optionally filtered by company name and date range.
     *
     * @param companyName The name of the company to filter by. Can be null or empty for no company filter.
     * @param startDate   The start date string (e.g., "YYYY-MM-DD") for filtering orders by 'made_at'.
     * @param endDate     The end date string (e.g., "YYYY-MM-DD") for filtering orders by 'made_at'.
     * @return The total number of orders matching the criteria.
     */
    public int getTotalOrders(String companyName, String startDate, String endDate) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM orders o");
        List<Object> params = new ArrayList<>();
        // Start with false as no WHERE clause is initially present in the base SQL
        appendCommonFilters(sql, params, companyName, startDate, endDate, "o.made_at", false);

        try (Connection conn = DBProvider.getConnection(Role.SYSTEM);
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching total orders: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retrieves the total number of completed orders, optionally filtered by company name and date range.
     *
     * @param companyName The name of the company to filter by. Can be null or empty for no company filter.
     * @param startDate   The start date string (e.g., "YYYY-MM-DD") for filtering orders by 'made_at'.
     * @param endDate     The end date string (e.g., "YYYY-MM-DD") for filtering orders by 'made_at'.
     * @return The total number of completed orders matching the criteria.
     */
    public int getCompletedOrders(String companyName, String startDate, String endDate) {
        StringBuilder sql =
                new StringBuilder("SELECT COUNT(*) FROM orders o WHERE status = 'completed'");
        List<Object> params = new ArrayList<>();
        // Pass true because 'WHERE status = 'completed'' is an existing WHERE clause
        appendCommonFilters(sql, params, companyName, startDate, endDate, "o.made_at", true);

        try (Connection conn = DBProvider.getConnection(Role.SYSTEM);
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching completed orders: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retrieves the order volume (count of orders) for a specified period (day, week, or month),
     * optionally filtered by company name and date range.
     *
     * @param period      The period to filter by ("day", "week", or "month"). Used if startDate/endDate are null.
     * @param companyName The name of the company to filter by. Can be null or empty for no company filter.
     * @param startDate   The start date string (e.g., "YYYY-MM-DD") for filtering orders by 'made_at'.
     *                    If provided, takes precedence over `period`.
     * @param endDate     The end date string (e.g., "YYYY-MM-DD") for filtering orders by 'made_at'.
     *                    If provided, takes precedence over `period`.
     * @return The number of orders within the specified period and criteria.
     * @throws IllegalArgumentException If an invalid period is provided and no date range is given.
     */
    public int getOrderVolumeByPeriod(String period, String companyName, String startDate, String endDate) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS count FROM orders o");
        List<Object> params = new ArrayList<>();

        // Determine the effective startDate and endDate based on period if explicit dates are not provided
        String effectiveStartDate = startDate;
        String effectiveEndDate = endDate;

        // Current date/time for calculations
        LocalDate now = LocalDate.now(); // Current date is 2025-07-02

        // If no explicit date range is provided, calculate the range for the requested period
        if ((startDate == null || startDate.trim().isEmpty()) && (endDate == null || endDate.trim().isEmpty())) {
            switch (period.toLowerCase()) {
                case "day":
                    effectiveStartDate = now.format(DateTimeFormatter.ISO_LOCAL_DATE); // 2025-07-02
                    effectiveEndDate = now.format(DateTimeFormatter.ISO_LOCAL_DATE); // 2025-07-02
                    break;
                case "week":
                    // Start of the current week (Monday)
                    effectiveStartDate = now.with(java.time.DayOfWeek.MONDAY).format(DateTimeFormatter.ISO_LOCAL_DATE); // 2025-06-30
                    effectiveEndDate = now.with(java.time.DayOfWeek.SUNDAY).format(DateTimeFormatter.ISO_LOCAL_DATE); // 2025-07-06
                    break;
                case "month":
                    // Start of the current month (July 1st)
                    effectiveStartDate = now.withDayOfMonth(1).format(DateTimeFormatter.ISO_LOCAL_DATE); // 2025-07-01
                    // End of the current month (July 31st)
                    effectiveEndDate = now.withDayOfMonth(now.lengthOfMonth()).format(DateTimeFormatter.ISO_LOCAL_DATE); // 2025-07-31
                    break;
                default:
                    // If an invalid period and no explicit dates, we might want to default or throw
                    // For now, let's just make sure it's handled. Returning 0 or throwing is fine.
                    System.err.println("Invalid period provided: " + period + " and no explicit date range.");
                    return 0; // Or throw new IllegalArgumentException("Invalid period: " + period);
            }
        }

        // Now, let appendCommonFilters handle ALL date filtering, using our effective dates
        // It starts with false because our base SQL doesn't have a WHERE clause yet.
        appendCommonFilters(sql, params, companyName, effectiveStartDate, effectiveEndDate, "o.made_at", false);

        try (Connection conn = DBProvider.getConnection(Role.SYSTEM);
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            setParameters(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching order volume: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retrieves the total number of completed trips (orders with status 'completed'),
     * optionally filtered by company name and date range.
     *
     * @param companyName The name of the company to filter by. Can be null or empty for no company filter.
     * @param startDate   The start date string (e.g., "YYYY-MM-DD") for filtering orders by 'made_at'.
     * @param endDate     The end date string (e.g., "YYYY-MM-DD") for filtering orders by 'made_at'.
     * @return The total number of completed trips matching the criteria.
     */
    public int getTotalTrips(String companyName, String startDate, String endDate) {
        StringBuilder sql =
                new StringBuilder("SELECT COUNT(*) FROM orders o WHERE o.status = 'completed'");
        List<Object> params = new ArrayList<>();
        // Pass true because of existing WHERE clause
        appendCommonFilters(sql, params, companyName, startDate, endDate, "o.made_at", true);

        try (Connection conn = DBProvider.getConnection(Role.SYSTEM);
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching total trips: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retrieves the total distance traveled for all completed orders,
     * optionally filtered by company name and date range.
     *
     * @param companyName The name of the company to filter by. Can be null or empty for no company filter.
     * @param startDate   The start date string (e.g., "YYYY-MM-DD") for filtering orders by 'made_at'.
     * @param endDate     The end date string (e.g., "YYYY-MM-DD") for filtering orders by 'made_at'.
     * @return The total distance traveled in kilometers.
     */
    public double getTotalDistanceTraveled(String companyName, String startDate, String endDate) {
        StringBuilder sql = new StringBuilder("""
                    SELECT COALESCE(SUM(v.distance), 0)
                    FROM orders o
                    JOIN vehicles v ON o.vehicle_id = v.vehicle_id 
                    WHERE o.status = 'completed'
                """);
        List<Object> params = new ArrayList<>();
        appendCommonFilters(sql, params, companyName, startDate, endDate, "o.made_at", true);

        try (Connection conn = DBProvider.getConnection(Role.SYSTEM);
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching total distance: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retrieves the total traveling hours for all completed orders,
     * calculated as the difference between ETA and made_at timestamps.
     * Optionally filtered by company name and date range.
     *
     * @param companyName The name of the company to filter by. Can be null or empty for no company filter.
     * @param startDate   The start date string (e.g., "YYYY-MM-DD") for filtering orders by 'made_at'.
     * @param endDate     The end date string (e.g., "YYYY-MM-DD") for filtering orders by 'made_at'.
     * @return The total traveling hours.
     */
    public double getTotalTravelingHours(String companyName, String startDate, String endDate) {
        StringBuilder sql = new StringBuilder("""
                    SELECT COALESCE(SUM(EXTRACT(EPOCH FROM (o.eta - o.made_at)) / 3600), 0)
                    FROM orders o
                    WHERE o.status = 'completed'
                """);
        List<Object> params = new ArrayList<>();
        // Pass true because of existing WHERE clause
        appendCommonFilters(sql, params, companyName, startDate, endDate, "o.made_at", true);

        try (Connection conn = DBProvider.getConnection(Role.SYSTEM);
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching traveling hours: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retrieves the average waiting time in minutes for all completed orders.
     * Optionally filtered by company name and date range.
     *
     * @param companyName The name of the company to filter by. Can be null or empty for no company filter.
     * @param startDate   The start date string (e.g., "YYYY-MM-DD") for filtering orders by 'made_at'.
     * @param endDate     The end date string (e.g., "YYYY-MM-DD") for filtering orders by 'made_at'.
     * @return The average waiting time in minutes.
     */
    public double getAverageWaitingTimeMinutes(String companyName, String startDate,
                                               String endDate) {
        StringBuilder sql = new StringBuilder("""
                    SELECT COALESCE(AVG(EXTRACT(EPOCH FROM (o.departure_time - o.tw_start)) / 60), 0)
                    FROM orders o
                    WHERE o.departure_time IS NOT NULL AND o.tw_start IS NOT NULL AND o.status = 'completed'
                """);
        List<Object> params = new ArrayList<>();
        appendCommonFilters(sql, params, companyName, startDate, endDate, "o.made_at", true);

        try (Connection conn = DBProvider.getConnection(Role.SYSTEM);
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching average waiting time: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retrieves the average freight weight per completed shipment.
     * Optionally filtered by company name and date range.
     *
     * @param companyName The name of the company to filter by. Can be null or empty for no company filter.
     * @param startDate   The start date string (e.g., "YYYY-MM-DD") for filtering orders by 'made_at'.
     * @param endDate     The end date string (e.g., "YYYY-MM-DD") for filtering orders by 'made_at'.
     * @return The average freight weight.
     */
    public double getAverageWeightPerShipment(String companyName, String startDate, String endDate) {
        StringBuilder sql = new StringBuilder("SELECT COALESCE(AVG(o.freight_weight), 0) FROM orders o WHERE o.freight_weight IS NOT NULL AND o.status = 'completed'");
        List<Object> params = new ArrayList<>();
        // Pass true because of existing WHERE clause
        appendCommonFilters(sql, params, companyName, startDate, endDate, "o.made_at", true);

        try (Connection conn = DBProvider.getConnection(Role.SYSTEM);
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching avg weight: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retrieves the count of completed orders that used shared containers.
     * Optionally filtered by company name and date range.
     *
     * @param companyName The name of the company to filter by. Can be null or empty for no company filter.
     * @param startDate   The start date string (e.g., "YYYY-MM-DD") for filtering orders by 'made_at'.
     * @param endDate     The end date string (e.g., "YYYY-MM-DD") for filtering orders by 'made_at'.
     * @return The count of shared containers in completed orders.
     */
    public int getSharedContainersCount(String companyName, String startDate, String endDate) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM orders o WHERE o.is_shared = true AND o.status = 'completed'");
        List<Object> params = new ArrayList<>();
        // Pass true because of existing WHERE clause
        appendCommonFilters(sql, params, companyName, startDate, endDate, "o.made_at", true);

        try (Connection conn = DBProvider.getConnection(Role.SYSTEM);
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching shared container count: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retrieves the composition of freight types for all completed orders.
     * Optionally filtered by company name and date range.
     *
     * @param companyName The name of the company to filter by. Can be null or empty for no company filter.
     * @param startDate   The start date string (e.g., "YYYY-MM-DD") for filtering orders by 'made_at'.
     * @param endDate     The end date string (e.g., "YYYY-MM-DD") for filtering orders by 'made_at'.
     * @return A Map where keys are FreightType enums and values are their respective counts.
     * Includes all known FreightType values, with a count of 0 if no orders of that type are found.
     * Unknown freight types from the database are mapped to `FreightType.other`.
     */
    public Map<FreightType, Integer> getFreightTypeComposition(String companyName, String startDate,
                                                               String endDate) {
        Map<FreightType, Integer> result = new HashMap<>();
        // Initialize all known freight types to 0 to ensure they are always in the map
        for (FreightType type : FreightType.values()) {
            result.put(type, 0); // Initialize using the enum constant itself
        }

        StringBuilder sql = new StringBuilder("""
                    SELECT freight_type, COUNT(*) AS count
                    FROM orders o
                    WHERE o.status = 'completed'
                """);
        List<Object> params = new ArrayList<>();
        appendCommonFilters(sql, params, companyName, startDate, endDate, "o.made_at", true);
        sql.append(" GROUP BY freight_type");

        try (Connection conn = DBProvider.getConnection(Role.SYSTEM);
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String typeString = rs.getString("freight_type");

                    if (typeString == null || typeString.trim().isEmpty()) {
                        System.err.println("Skipping null or empty freight_type from database. Mapping to 'other'.");
                        // Increment 'other' for null/empty values
                        result.put(FreightType.other, result.getOrDefault(FreightType.other, 0) + rs.getInt("count"));
                        continue; // Move to the next result row
                    }

                    try {
                        FreightType ft = FreightType.fromDbValue(typeString);
                        result.put(ft, rs.getInt("count"));
                    } catch (IllegalArgumentException e) {
                        // This catch block will now only be hit if fromDbValue fails (i.e., truly unknown string)
                        System.err.println("Unknown freight type from database: '" + typeString + "'. Mapping to 'other'.");
                        result.put(FreightType.other,
                                result.getOrDefault(FreightType.other, 0) + rs.getInt("count"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching freight type composition: " + e.getMessage());
        }
        return result;
    }

    /**
     * Retrieves the total freight value of all orders, optionally filtered by company name and date range.
     *
     * @param companyName The name of the company to filter by. Can be null or empty for no company filter.
     * @param startDate   The start date string (e.g., "YYYY-MM-DD") for filtering orders by 'made_at'.
     * @param endDate     The end date string (e.g., "YYYY-MM-DD") for filtering orders by 'made_at'.
     * @return The total freight value.
     */
    public double getTotalFreightValue(String companyName, String startDate, String endDate) {
        StringBuilder sql = new StringBuilder("SELECT COALESCE(SUM(freight_value), 0) FROM orders o");
        List<Object> params = new ArrayList<>();
        // Start with false as no WHERE clause is initially present in the base SQL
        appendCommonFilters(sql, params, companyName, startDate, endDate, "o.made_at", false);

        try (Connection conn = DBProvider.getConnection(Role.SYSTEM);
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching total freight value: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retrieves aggregated order data for a line chart, grouped by year, month, or day.
     * Optionally filtered by company name and a specific date range.
     *
     * @param period       The aggregation period ("year", "month", or "day").
     * @param companyName  The name of the company to filter by. Can be null or empty for no company filter.
     * @param startDateStr The start date string (e.g., "YYYY-MM-DD") for filtering orders by 'made_at'.
     * @param endDateStr   The end date string (e.g., "YYYY-MM-DD") for filtering orders by 'made_at'.
     * @return A Map containing two lists: "labels" (String) and "data" (Integer),
     * representing the time labels and corresponding order counts.
     * The map will be empty if no data is found or an error occurs.
     * @throws IllegalArgumentException If an invalid period is provided.
     */
    public Map<String, Object> getOrdersDataForChart(String period, String companyName,
                                                     String startDateStr, String endDateStr) {
        Map<String, Object> result = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        // Use TreeMap to store data temporarily to ensure labels are sorted chronologically
        // Key is the sortable identifier (e.g., YYYY-MM-DD, YYYY-MM)
        // Value is the order count
        TreeMap<String, Integer> rawDataMap = new TreeMap<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM"); // e.g., Jan, Feb
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd"); // e.g., Jan 01, Feb 15

        String selectClause;
        String groupByClause;
        String orderByClause;

        switch (period.toLowerCase()) {
            case "year":
                selectClause = "EXTRACT(YEAR FROM o.made_at) AS year_label";
                groupByClause = "year_label";
                orderByClause = "year_label";
                break;
            case "month":
                selectClause = "date_trunc('month', o.made_at) AS month_key";
                groupByClause = "month_key";
                orderByClause = "month_key";
                break;
            case "day":
                selectClause = "date_trunc('day', o.made_at) AS day_key";
                groupByClause = "day_key";
                orderByClause = "day_key";
                break;
            default:
                System.err.println("Invalid period for chart data: " + period);
                // Throwing here might be better to propagate the error, or handle gracefully on frontend
                return result; // Return empty result for invalid period
        }

        StringBuilder sql = new StringBuilder(String.format(
                "SELECT %s, COUNT(*) AS order_count FROM orders o", selectClause));

        List<Object> params = new ArrayList<>();
        // Start with false as no WHERE clause is initially present in the base SQL
        // appendCommonFilters will add WHERE or AND as needed
        appendCommonFilters(sql, params, companyName, startDateStr, endDateStr, "o.made_at", false);

        sql.append(" GROUP BY ").append(groupByClause);
        sql.append(" ORDER BY ").append(orderByClause);


        try (Connection conn = DBProvider.getConnection(Role.SYSTEM);
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String key;
                    if ("year".equals(period)) {
                        key = String.valueOf(rs.getInt("year_label"));
                    } else if ("month".equals(period)) {
                        Timestamp ts = rs.getTimestamp("month_key");
                        // Format to YYYY-MM for consistent sorting key
                        key = ts.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                    } else { // "day"
                        Timestamp ts = rs.getTimestamp("day_key");
                        // Format to YYYY-MM-DD for consistent sorting key
                        key = ts.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    }
                    rawDataMap.put(key, rs.getInt("order_count"));
                }
            }

            // Populate labels and data from the sorted TreeMap
            for (Map.Entry<String, Integer> entry : rawDataMap.entrySet()) {
                String rawKey = entry.getKey();
                Integer count = entry.getValue();

                String displayLabel;
                if ("year".equals(period)) {
                    displayLabel = rawKey; // Year is already in desired format
                } else if ("month".equals(period)) {
                    // Parse YYYY-MM key to LocalDate (e.g., 2025-01-01) then format to "MMM" (Jan)
                    LocalDate date = LocalDate.parse(rawKey + "-01");
                    displayLabel = date.format(monthFormatter);
                } else { // "day"
                    // Parse YYYY-MM-DD key to LocalDate then format to "MMM dd" (Jan 01)
                    LocalDate date = LocalDate.parse(rawKey);
                    displayLabel = date.format(dateFormatter);
                }
                labels.add(displayLabel);
                data.add(count);
            }

            result.put("labels", labels);
            result.put("data", data);

        } catch (SQLException e) {
            System.err.println("Error fetching orders data for chart (" + period + "): " + e.getMessage());
        }
        return result;
    }
}