package org.saved4.saved4.dto;

import org.postgresql.geometric.PGpoint;
import org.saved4.saved4.enums.VehicleStatus;

public class Vehicle {
    private int vehicleId;
    private PGpoint position;
    private float battery_level;
    private VehicleStatus status;
    private float distance;

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    @Override
    public String toString() {
        return "Vehicle{" + "id=" + vehicleId + ", position=" + position + ", battery_level=" +
                battery_level + ", status=" + status + ", distance=" + distance + '}';
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public PGpoint getPosition() {
        return position;
    }

    public void setPosition(PGpoint position) {
        this.position = position;
    }

    public float getBattery_level() {
        return battery_level;
    }

    public void setBattery_level(float battery_level) {
        this.battery_level = battery_level;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

   
}
