package org.saved4.saved4.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ContainerStatus {
    available("available"), in_use("in_use"), broken("broken");

    private final String dbValue;

    ContainerStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }

    public static ContainerStatus fromDbValue(String dbValue) {
        for (ContainerStatus status : ContainerStatus.values()) {
            if (status.dbValue.equalsIgnoreCase(dbValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No VehicleStatus with dbValue: " + dbValue);
    }
}
