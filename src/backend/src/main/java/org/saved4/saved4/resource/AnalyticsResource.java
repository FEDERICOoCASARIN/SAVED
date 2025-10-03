package org.saved4.saved4.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.saved4.saved4.dto.AnalyticsStats;
import org.saved4.saved4.service.AnalyticsService;

import java.util.Map;

/**
 * RESTful resource for providing analytics data.
 * This class exposes endpoints to retrieve various statistics related to
 * vehicles, orders, shipments, and container utilization, with optional filtering capabilities.
 */
@Path("/analytics")
public class AnalyticsResource {

    @Inject
    private AnalyticsService analyticsService;

    /**
     * Retrieves an overview of vehicle statistics.
     *
     * @return An {@link AnalyticsStats} object containing vehicle-related statistics.
     * @throws WebApplicationException If an error occurs during data retrieval.
     */
    @GET
    @Path("/vehicle-overview")
    @Produces(MediaType.APPLICATION_JSON)
    public AnalyticsStats getVehicleOverview() {
        try {
            return analyticsService.getVehicleOverview();
        } catch (Exception e) {
            throw new WebApplicationException("Failed to retrieve vehicle overview: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves an overview of order statistics for a specified period.
     *
     * @param period The period for order volume calculation (e.g., "day", "week", or "month").
     *               Defaults to "day" if not provided.
     * @return An {@link AnalyticsStats} object containing order-related statistics.
     * @throws WebApplicationException If an invalid period is provided or an error occurs during data retrieval.
     */
    @GET
    @Path("/order-overview")
    @Produces(MediaType.APPLICATION_JSON)
    public AnalyticsStats getOrderOverview(@QueryParam("period") @DefaultValue("day") String period) {
        try {
            if (!period.equals("day") && !period.equals("week") && !period.equals("month")) {
                throw new WebApplicationException("Invalid period. Use 'day', 'week', or 'month'.", Response.Status.BAD_REQUEST);
            }
            return analyticsService.getOrderOverview(period);
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new WebApplicationException("Failed to retrieve order overview: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves an overview of shipment statistics.
     *
     * @return An {@link AnalyticsStats} object containing shipment-related statistics.
     * @throws WebApplicationException If an error occurs during data retrieval.
     */
    @GET
    @Path("/shipment-overview")
    @Produces(MediaType.APPLICATION_JSON)
    public AnalyticsStats getShipmentOverview() {
        try {
            return analyticsService.getShipmentOverview();
        } catch (Exception e) {
            throw new WebApplicationException("Failed to retrieve shipment overview: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves an overview of container utilization statistics.
     *
     * @return An {@link AnalyticsStats} object containing container utilization statistics.
     * @throws WebApplicationException If an error occurs during data retrieval.
     */
    @GET
    @Path("/container-utilization")
    @Produces(MediaType.APPLICATION_JSON)
    public AnalyticsStats getContainerUtilization() {
        try {
            return analyticsService.getContainerUtilization();
        } catch (Exception e) {
            throw new WebApplicationException("Failed to retrieve container utilization: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves aggregated order data for the line chart, grouped by the specified period (year, month, or day).
     *
     * @param period      The aggregation period ("year", "month", or "day"). Defaults to "year" if not provided.
     * @param companyName The name of the company to filter by. Can be null for no company filter.
     * @param startDate   The start date string (e.g., "YYYY-MM-DD") for filtering. Can be null for no start date filter.
     * @param endDate     The end date string (e.g., "YYYY-MM-DD") for filtering. Can be null for no end date filter.
     * @return A Map containing "labels" (List<String>) and "data" (List<Integer>) for the chart.
     * @throws WebApplicationException If an invalid period is provided or an error occurs during data retrieval.
     */
    @GET
    @Path("/orders-by-period") // New endpoint for chart data
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getOrdersChartData(
            @QueryParam("period") @DefaultValue("year") String period, // Default to 'year' for the chart
            @QueryParam("companyName") String companyName,
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate
    ) {
        try {
            // Validate the period parameter for the chart
            if (!period.equals("year") && !period.equals("month") && !period.equals("day")) {
                throw new WebApplicationException("Invalid period. Use 'year', 'month', or 'day'.", Response.Status.BAD_REQUEST);
            }
            return analyticsService.getOrdersDataForChart(period, companyName, startDate, endDate);
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new WebApplicationException("Failed to retrieve chart data: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves all analytics statistics, applying optional filters for company name and date range.
     *
     * @param period      The period for order volume calculation (e.g., "day", "week", or "month").
     *                    Defaults to "day" if not provided.
     * @param companyName The name of the company to filter by. Can be null for no company filter.
     * @param startDate   The start date string (e.g., "YYYY-MM-DD") for filtering. Can be null for no start date filter.
     * @param endDate     The end date string (e.g., "YYYY-MM-DD") for filtering. Can be null for no end date filter.
     * @return An {@link AnalyticsStats} object containing a comprehensive set of analytics data.
     * @throws WebApplicationException If an invalid period is provided or an error occurs during data retrieval.
     */
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public AnalyticsStats getAllAnalytics(
            @QueryParam("period") @DefaultValue("day") String period,
            @QueryParam("companyName") String companyName,
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate
    ) {
        try {
            if (!period.equals("day") && !period.equals("week") && !period.equals("month")) {
                throw new WebApplicationException("Invalid period. Use 'day', 'week', or 'month'.", Response.Status.BAD_REQUEST);
            }
            return analyticsService.getAllAnalytics(period, companyName, startDate, endDate);
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new WebApplicationException("Failed to retrieve all analytics: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves analytics statistics specific to a single user (company), applying optional date filters.
     *
     * @param companyName The name of the company for which to retrieve analytics. Can be null for no company filter.
     * @param startDate   The start date string (e.g., "YYYY-MM-DD") for filtering. Can be null for no start date filter.
     * @param endDate     The end date string (e.g., "YYYY-MM-DD") for filtering. Can be null for no end date filter.
     * @return An {@link AnalyticsStats} object containing user-specific analytics data.
     * @throws WebApplicationException If an error occurs during data retrieval.
     */
    @GET
    @Path("/user-analytics")
    @Produces(MediaType.APPLICATION_JSON)
    public AnalyticsStats getUserAnalytics(
            @QueryParam("companyName") String companyName,
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate
    ) {
        try {
            return analyticsService.getUserAnalytics(companyName, startDate, endDate);
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new WebApplicationException("Failed to retrieve user analytics: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
