package com.poortoys.examples.entities;

// BookingStatus.java
public enum BookingStatus {
    IN_PROGRESS("IN_PROGRESS"),
    CONFIRMED("CONFIRMED"),
    CANCELED("CANCELED");    // Single 'L' spelling

    private final String value;

    BookingStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    // Custom converter method to handle possible database value mismatches
    public static BookingStatus fromString(String status) {
        if (status == null) {
            return null;
        }
        
        // Normalize the input string
        String normalizedStatus = status.toUpperCase()
                                      .trim()
                                      .replace(" ", "_");

        // Handle common variations
        switch (normalizedStatus) {
            case "IN_PROGRESS":
            case "INPROGRESS":
            case "IN PROGRESS":
                return IN_PROGRESS;
            case "CONFIRMED":
            case "CONFIRM":
                return CONFIRMED;
            case "CANCELED":
            case "CANCELLED":  // Handle British spelling
            case "CANCEL":
                return CANCELED;
            default:
                throw new IllegalArgumentException("Invalid booking status: " + status);
        }
    }
}