package org.saved4.saved4.service;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.saved4.saved4.dao.AnalyticsDao;
import org.saved4.saved4.dto.AnalyticsStats;

import java.util.Map;

/**
 * Service class for retrieving and aggregating analytics data.
 * It interacts with the AnalyticsDao to fetch raw data and
 * provides higher-level methods to get categorized statistics.
 */
@RequestScoped
public class AnalyticsService {
    @Inject
    private AnalyticsDao analyticsDAO;

    /**
     * Retrieves an overview of order statistics for a specified period.
     *
     * @param period The period for order volume calculation (e.g., "day", "week", or "month").
     * @return An AnalyticsStats object containing total orders, completed orders, completion rate, and order volume.
     */
    public AnalyticsStats getOrderOverview(String period) {
        AnalyticsStats stats = new AnalyticsStats();
        stats.setTotalOrders(analyticsDAO.getTotalOrders(null, null, null));
        stats.setCompletedOrders(analyticsDAO.getCompletedOrders(null, null, null));

        int total = stats.getTotalOrders();
        int completed = stats.getCompletedOrders();
        if (total > 0) {
            stats.setCompletionRate((double) completed / total * 100.0);
        } else {
            stats.setCompletionRate(0.0);
        }
        stats.setOrderVolume(analyticsDAO.getOrderVolumeByPeriod(period, null, null, null));
        return stats;
    }

    /**
     * Retrieves an overview of vehicle statistics.
     *
     * @return An AnalyticsStats object containing total vehicles, available vehicles, and vehicle status distribution.
     */
    public AnalyticsStats getVehicleOverview() {
        AnalyticsStats stats = new AnalyticsStats();
        stats.setTotalVehicles(analyticsDAO.getTotalVehicleCount());
        stats.setAvailableVehicles(analyticsDAO.getAvailableVehicles());
        stats.setVehicleStatusDistribution(analyticsDAO.getVehicleStatusDistribution());
        return stats;
    }

    /**
     * Retrieves an overview of shipment statistics.
     *
     * @return An AnalyticsStats object containing total trips, total distance traveled, traveling hours, and average waiting time.
     */
    public AnalyticsStats getShipmentOverview() {
        AnalyticsStats stats = new AnalyticsStats();
        stats.setTotalTrips(analyticsDAO.getTotalTrips(null, null, null));
        stats.setTotalDistance(analyticsDAO.getTotalDistanceTraveled(null, null, null));
        stats.setTravelingHours(analyticsDAO.getTotalTravelingHours(null, null, null));
        stats.setAverageWaitingTime(analyticsDAO.getAverageWaitingTimeMinutes(null, null, null));
        return stats;
    }

    /**
     * Retrieves an overview of container utilization statistics.
     *
     * @return An AnalyticsStats object containing average weight per shipment, shared containers count, and freight type composition.
     */
    public AnalyticsStats getContainerUtilization() {
        AnalyticsStats stats = new AnalyticsStats();
        stats.setAvgWeightPerShipment(analyticsDAO.getAverageWeightPerShipment(null, null, null));
        stats.setSharedContainers(analyticsDAO.getSharedContainersCount(null, null, null));
        stats.setFreightTypeComposition(analyticsDAO.getFreightTypeComposition(null, null, null));
        return stats;
    }

    /**
     * Retrieves aggregated order data for the line chart, grouped by the specified period (year, month, or day).
     *
     * @param period      The aggregation period ("year", "month", or "day").
     * @param companyName The name of the company to filter by. Can be null or empty for no company filter.
     * @param startDate   The start date string (e.g., "YYYY-MM-DD") for filtering orders by 'made_at'.
     * @param endDate     The end date string (e.g., "YYYY-MM-DD") for filtering orders by 'made_at'.
     * @return A Map containing two lists: "labels" (String) and "data" (Integer),
     * representing the time labels and corresponding order counts.
     */
    public Map<String, Object> getOrdersDataForChart(String period, String companyName,
                                                     String startDate, String endDate) {
        return analyticsDAO.getOrdersDataForChart(period, companyName, startDate, endDate);
    }

    /**
     * Retrieves all analytics statistics, applying optional filters for company name and date range.
     *
     * @param period      The period for order volume calculation (e.g., "day", "week", or "month").
     * @param companyName The name of the company to filter by. Can be null or empty for no company filter.
     * @param startDate   The start date string (e.g., "YYYY-MM-DD") for filtering.
     * @param endDate     The end date string (e.g., "YYYY-MM-DD") for filtering.
     * @return An AnalyticsStats object containing a comprehensive set of analytics data.
     */
    public AnalyticsStats getAllAnalytics(String period, String companyName, String startDate, String endDate) {
        AnalyticsStats stats = new AnalyticsStats();

        // Vehicle Overview
        stats.setTotalVehicles(analyticsDAO.getTotalVehicleCount());
        stats.setAvailableVehicles(analyticsDAO.getAvailableVehicles());
        stats.setVehicleStatusDistribution(analyticsDAO.getVehicleStatusDistribution());

        // Order Overview
        stats.setTotalOrders(analyticsDAO.getTotalOrders(companyName, startDate, endDate));
        stats.setCompletedOrders(analyticsDAO.getCompletedOrders(companyName, startDate, endDate));
        int total = stats.getTotalOrders();
        int completed = stats.getCompletedOrders();
        if (total > 0) {
            stats.setCompletionRate((double) completed / total * 100.0);
        } else {
            stats.setCompletionRate(0.0);
        }
        stats.setOrderVolume(analyticsDAO.getOrderVolumeByPeriod(period, companyName, startDate, endDate));

        // Shipment Overview
        stats.setTotalTrips(analyticsDAO.getTotalTrips(companyName, startDate, endDate));
        stats.setTotalDistance(analyticsDAO.getTotalDistanceTraveled(companyName, startDate, endDate));
        stats.setTravelingHours(analyticsDAO.getTotalTravelingHours(companyName, startDate, endDate));
        stats.setAverageWaitingTime(analyticsDAO.getAverageWaitingTimeMinutes(companyName, startDate, endDate));

        // Container Utilization
        stats.setAvgWeightPerShipment(analyticsDAO.getAverageWeightPerShipment(companyName, startDate, endDate));
        stats.setSharedContainers(analyticsDAO.getSharedContainersCount(companyName, startDate, endDate));
        stats.setFreightTypeComposition(analyticsDAO.getFreightTypeComposition(companyName, startDate, endDate));

        return stats;
    }

    /**
     * Retrieves analytics statistics specific to a single user (company), applying optional date filters.
     *
     * @param companyName The name of the company for which to retrieve analytics.
     * @param startDate   The start date string (e.g., "YYYY-MM-DD") for filtering.
     * @param endDate     The end date string (e.g., "YYYY-MM-DD") for filtering.
     * @return An AnalyticsStats object containing user-specific analytics data like total orders,
     * completed orders, total freight value, average weight per shipment, and completion rate.
     */
    public AnalyticsStats getUserAnalytics(String companyName, String startDate, String endDate) {
        AnalyticsStats stats = new AnalyticsStats();

        stats.setTotalOrders(analyticsDAO.getTotalOrders(companyName, startDate, endDate));
        stats.setCompletedOrders(analyticsDAO.getCompletedOrders(companyName, startDate, endDate));
        stats.setTotalFreightValue(analyticsDAO.getTotalFreightValue(companyName, startDate, endDate));
        stats.setAvgWeightPerShipment(analyticsDAO.getAverageWeightPerShipment(companyName, startDate, endDate));

        // Calculate completion rate for user stats
        int total = stats.getTotalOrders();
        int completed = stats.getCompletedOrders();
        if (total > 0) {
            stats.setCompletionRate((double) completed / total * 100.0);
        } else {
            stats.setCompletionRate(0.0);
        }

        return stats;
    }
}
