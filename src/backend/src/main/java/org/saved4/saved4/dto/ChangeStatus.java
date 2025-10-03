package org.saved4.saved4.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ChangeStatus {
//    created("created"),
    removed("removed"),
    edited("edited"),
    rescheduled("rescheduled");
    private final String dbValue;

    ChangeStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }

    public static ChangeStatus fromDbValue(String dbValue) {
        for (ChangeStatus type : ChangeStatus.values()) {
            if (type.dbValue.equals(dbValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No ChangeSatus with dbValue " + dbValue);
    }
}
