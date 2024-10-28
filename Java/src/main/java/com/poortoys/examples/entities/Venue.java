package com.poortoys.examples.entities;
import javax.persistence.*;


/**
 * Represents a venue where events take place.
 */
@Entity
@Table(name = "venues")
public class Venue {

    // Primary key of the venues table, auto-generated
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "venue_id")
    private int venueId;

    // Name of the venue, cannot be null, maximum length of 50 characters
    @Column(name = "venue_name", nullable = false, length = 50)
    private String venueName;

    // Address or location details of the venue, cannot be null, maximum length of 100 characters
    @Column(name = "location", nullable = false, length = 100)
    private String location;

    // Type of venue (e.g., Concert Hall, Stadium), maximum length of 30 characters
    @Column(name = "type", length = 30)
    private String type;

    // Maximum number of attendees the venue can hold, cannot be null
    @Column(name = "capacity", nullable = false)
    private int capacity;

    // Default constructor required by JPA
    public Venue() {
    }

    // Constructor for convenience
    public Venue(String venueName, String location, String type, int capacity) {
        this.venueName = venueName;
        this.location = location;
        this.type = type;
        this.capacity = capacity;
    }

    // Getter for venueId (no setter since it's auto-generated)
    public int getVenueId() {
        return venueId;
    }

    // Getter and setter for venueName
    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    // Getter and setter for location
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // Getter and setter for type
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // Getter and setter for capacity
    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    // Override toString() for better readability
    @Override
    public String toString() {
        return "Venue{" +
                "venueId=" + venueId +
                ", venueName='" + venueName + '\'' +
                ", location='" + location + '\'' +
                ", type='" + type + '\'' +
                ", capacity=" + capacity +
                '}';
    }
}