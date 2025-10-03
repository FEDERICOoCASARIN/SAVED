package org.saved4.saved4.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderStatus {
    created("created"),
    scheduled("scheduled"),
    undergoing("undergoing"),
    completed("completed"),
    canceled("canceled");

    private final String dbValue;

    OrderStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }

    public static OrderStatus fromDbValue(String dbValue) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.dbValue.equals(dbValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No OrderStatus with dbValue " + dbValue);
    }
}
