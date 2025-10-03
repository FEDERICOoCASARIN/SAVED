package org.saved4.saved4.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FreightType {
    machinery("machinery"), food("food"), chemical("chemical"), textile("textile"),
    electronic("electronic"), other("other");

    private final String dbValue;

    FreightType(String dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }

    public static FreightType fromDbValue(String dbValue) {
        for (FreightType type : FreightType.values()) {
            if (type.dbValue.equals(dbValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No OperationType with dbValue " + dbValue);
    }
}
