package com.poortoys.examples;


import javax.persistence.*;
import java.util.Date;

/**
 * Represents an event where performers take place at a venue.
 */
@Entity
@Table(name = "events")
public class Event {

    // Primary key of the events table, auto-generated
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private int eventId;

    // Name of the event, cannot be null, maximum length of 100 characters
    @Column(name = "event_name", nullable = false, length = 100)
    private String eventName;

    // Many events can have one performer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performer_id")
    private Performer performer;

    // Date of the event, cannot be null
    @Temporal(TemporalType.DATE)
    @Column(name = "event_date", nullable = false)
    private Date eventDate;

    // Many events can take place at one venue
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id")
    private Venue venue;

    // Default constructor required by JPA
    public Event() {
    }

    // Constructor for convenience
    public Event(String eventName, Performer performer, Date eventDate, Venue venue) {
        this.eventName = eventName;
        this.performer = performer;
        this.eventDate = eventDate;
        this.venue = venue;
    }

    // Getter for eventId (no setter since it's auto-generated)
    public int getEventId() {
        return eventId;
    }

    // Getter and setter for eventName
    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    // Getter and setter for performer
    public Performer getPerformer() {
        return performer;
    }

    public void setPerformer(Performer performer) {
        this.performer = performer;
    }

    // Getter and setter for eventDate
    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    // Getter and setter for venue
    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    //Override toString() for better readability
    @Override
    public String toString() {
        return "Event{" +
                "eventId=" + eventId +
                ", eventName='" + eventName + '\'' +
                ", performer=" + (performer != null ? performer.getPerformerName() : "null") +
                ", eventDate=" + eventDate +
                ", venue=" + (venue != null ? venue.getVenueName() : "null") +
                '}';
    }

 
}


