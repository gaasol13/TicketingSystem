package com.poortoys.examples.entities;

public enum TicketStatus {
    AVAILABLE("available"),
    SOLD("sold"),
    RESERVED("reserved");

    private final String dbValue;

    TicketStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    // Method to retrieve enum by database value
    public static TicketStatus fromDbValue(String dbValue) {
        for (TicketStatus status : TicketStatus.values()) {
            if (status.dbValue.equalsIgnoreCase(dbValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown database value: " + dbValue);
    }
}