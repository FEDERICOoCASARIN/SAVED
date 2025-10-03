package org.saved4.saved4.dto;

import java.util.Map;
import org.saved4.saved4.enums.FreightType;
import org.saved4.saved4.enums.VehicleStatus;

public class AnalyticsStats {
    private int totalOrders;
    private int completedOrders;
    private double completionRate;
    private int orderVolume;
    private int totalVehicles;
    private int availableVehicles;
    private Map<VehicleStatus, Integer> vehicleStatusDistribution;
    private int totalTrips;
    private double totalDistance;
    private double travelingHours;
    private double averageWaitingTime;
    private double avgWeightPerShipment;
    private int sharedContainers;
    private Map<FreightType, Integer> freightTypeComposition;
    private double totalFreightValue;

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public int getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(int completedOrders) {
        this.completedOrders = completedOrders;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(double completionRate) {
        this.completionRate = completionRate;
    }

    public int getOrderVolume() {
        return orderVolume;
    }

    public void setOrderVolume(int orderVolume) {
        this.orderVolume = orderVolume;
    }

    public int getTotalVehicles() {
        return totalVehicles;
    }

    public void setTotalVehicles(int totalVehicles) {
        this.totalVehicles = totalVehicles;
    }

    public int getAvailableVehicles() {
        return availableVehicles;
    }

    public void setAvailableVehicles(int availableVehicles) {
        this.availableVehicles = availableVehicles;
    }

    public Map<VehicleStatus, Integer> getVehicleStatusDistribution() {
        return vehicleStatusDistribution;
    }

    public void setVehicleStatusDistribution(
            Map<VehicleStatus, Integer> vehicleStatusDistribution) {
        this.vehicleStatusDistribution = vehicleStatusDistribution;
    }

    public Map<FreightType, Integer> getFreightTypeComposition() {
        return freightTypeComposition;
    }

    public void setFreightTypeComposition(Map<FreightType, Integer> freightTypeComposition) {
        this.freightTypeComposition = freightTypeComposition;
    }

    public int getSharedContainers() {
        return sharedContainers;
    }

    public void setSharedContainers(int sharedContainers) {
        this.sharedContainers = sharedContainers;
    }

    public double getAvgWeightPerShipment() {
        return avgWeightPerShipment;
    }

    public void setAvgWeightPerShipment(double avgWeightPerShipment) {
        this.avgWeightPerShipment = avgWeightPerShipment;
    }

    public double getAverageWaitingTime() {
        return averageWaitingTime;
    }

    public void setAverageWaitingTime(double averageWaitingTime) {
        this.averageWaitingTime = averageWaitingTime;
    }

    public double getTravelingHours() {
        return travelingHours;
    }

    public void setTravelingHours(double travelingHours) {
        this.travelingHours = travelingHours;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public int getTotalTrips() {
        return totalTrips;
    }

    public void setTotalTrips(int totalTrips) {
        this.totalTrips = totalTrips;
    }

    public double getTotalFreightValue() {
        return totalFreightValue;
    }

    public void setTotalFreightValue(double totalFreightValue) {
        this.totalFreightValue = totalFreightValue;
    }
}
