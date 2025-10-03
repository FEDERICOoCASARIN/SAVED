package org.saved4.saved4.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OperationType {
    loading("loading"), unloading("unloading");

    private final String dbValue;

    OperationType(String dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }

    public static OperationType fromDbValue(String dbValue) {
        for (OperationType type : OperationType.values()) {
            if (type.dbValue.equals(dbValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No OperationType with dbValue " + dbValue);
    }
}
