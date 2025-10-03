package org.saved4.saved4.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum VehicleStatus {
    available("available"), in_use("in_use"), out_of_order("out_of_order");

    private final String dbValue;

    VehicleStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }

    public static VehicleStatus fromDbValue(String dbValue) {
        for (VehicleStatus status : VehicleStatus.values()) {
            if (status.dbValue.equalsIgnoreCase(dbValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No VehicleStatus with dbValue: " + dbValue);
    }
}
