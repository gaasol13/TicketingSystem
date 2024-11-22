package com.ticketing.system.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;


import dev.morphia.annotations.Entity;
import dev.morphia.annotations.*;
import dev.morphia.annotations.Indexes;
import dev.morphia.annotations.Embedded;

@Entity("events") // Specifies the MongoDB collection name as "events"
@Indexes({
    @Index(fields = {@Field("event_name"), @Field("date")}, options = @IndexOptions(unique = true, name = "name_date_idx")),
    @Index(fields = @Field("performer"), options = @IndexOptions(name = "performer_idx")),
    @Index(fields = @Field("venue"), options = @IndexOptions(name = "venue_idx"))
})
public class Event {
	
	@Id
	private ObjectId id; //mongos unique identifier
	
	@Property("event_name")
	private String name;
	
	@Reference(lazy = true)//References the performar document, loaded lazily
	private Set<Performer> performers;
	
	@Property("date")
	private Date date; //date of the event
	
	@Reference(lazy = true)
	private Venue venue; //References the venue document
	
	//("ticketCategories") //embeds a list of TicketCategory documents within the Event document
	@Property("ticketCategories")
	private Set<TicketCategory> ticketCategories; //list of ticket categories for the event
	
    public Event() {
    	this.ticketCategories = new HashSet<>();
    }


    public Event(String name, Set<Performer> performer, Date date, Venue venue, Set<TicketCategory> ticketCategories) {
        this.name = name;
        this.performers = performers;
        this.date = date;
        this.venue = venue;
        this.ticketCategories = (ticketCategories != null) ? ticketCategories : new HashSet<>();
    }


    public ObjectId getId() {
        return id;
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public Set<Performer> getPerformers() {
		return performers;
	}

    public void setPerformers(Set<Performer> performers) {
        this.performers = performers;
    }

    //Gets the date of the event.
    public Date getDate() {
        return date;
    }

    // Sets the date for the event.
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Gets the venue where the event is held.
     */
    public Venue getVenue() {
        return venue;
    }

    /**
     * Sets the venue for the event.
     */
    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    /**
     * Gets the list of ticket categories for the event.
     */
    public void setTicketCategories(Set<TicketCategory> ticketCategories) {
        this.ticketCategories = ticketCategories;
    }

  

    public Set<TicketCategory> getTicketCategories() {
		return ticketCategories;
	}


	/**
     * Overrides the default toString() method for better readability.
     */
    @Override
    public String toString() {
        StringBuilder performersNames = new StringBuilder();
        if (performers != null) {
            for (Performer p : performers) {
                performersNames.append(p.getPerformerName()).append(", ");
            }
            // Remove trailing comma and space
            if (performersNames.length() > 0) {
                performersNames.setLength(performersNames.length() - 2);
            }
        }

        return "Event{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", performers=[" + performersNames +
                "], date=" + date +
                ", venue=" + (venue != null ? venue.getVenueName() : "null") +
                ", ticketCategories=" + ticketCategories +
                '}';
    }

}
