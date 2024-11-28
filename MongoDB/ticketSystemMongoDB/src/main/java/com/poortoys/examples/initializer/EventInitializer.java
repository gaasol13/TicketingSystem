package com.poortoys.examples.initializer;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.poortoys.examples.dao.EventDAO;
import com.poortoys.examples.dao.PerformerDAO;
import com.poortoys.examples.dao.VenueDAO;
import com.ticketing.system.entities.Event;
import com.ticketing.system.entities.Performer;
import com.ticketing.system.entities.TicketCategory;
import com.ticketing.system.entities.Venue;



/**
 * Initializes Event entities and populates the database with sample data.
 * Each event is associated with multiple performers and a venue.
 * Ticket categories are handled separately by TicketCategoryInitializer.
 */
public class EventInitializer implements Initializer {

    // DAO instances for events, performers, and venues
    private final EventDAO eventDAO;
    private final PerformerDAO performerDAO;
    private final VenueDAO venueDAO;

    /**
     * Constructor that accepts DAOs for interacting with the database.
 
     */
    public EventInitializer(EventDAO eventDAO, PerformerDAO performerDAO, VenueDAO venueDAO) {
        this.eventDAO = eventDAO;
        this.performerDAO = performerDAO;
        this.venueDAO = venueDAO;
    }

@Override
public void initialize() {
    System.out.println("Initializing events...");

    // Define a list of events with their details
    List<EventData> events = Arrays.asList(
        // Event 1: Rock Fest 2024
        new EventData(
            "Rock Fest 2024",
            Arrays.asList("The Rockers"),
            parseDate("2024-06-15"),
            "Grand Arena"
        ),
        // Event 2: Jazz Nights
        new EventData(
            "Jazz Nights",
            Arrays.asList("Jazz Masters"),
            parseDate("2024-07-20"),
            "Jazz Club"
        ),
        // Event 3: Classical Evening
        new EventData(
            "Classical Evening",
            Arrays.asList("Classical Quartet"),
            parseDate("2024-08-05"),
            "Symphony Hall"
       /* ),
        // Event 4: Pop Extravaganza
        new EventData(
            "Pop Extravaganza",
            Arrays.asList("Pop Icons"),
            parseDate("2024-09-10"),
            "Pop Dome"
        ),
        // Event 5: Electronic Beats
        new EventData(
            "Electronic Beats",
            Arrays.asList("Electronica"),
            parseDate("2024-10-25"),
            "Electronica Center"
        ),
        // Event 6: Hip-Hop Bash
        new EventData(
            "Hip-Hop Bash",
            Arrays.asList("Hip-Hop Crew"),
            parseDate("2024-11-15"),
            "Hip-Hop Venue"
        ),
        // Event 7: Country Fair
        new EventData(
            "Country Fair",
            Arrays.asList("Country Stars"),
            parseDate("2024-12-05"),
            "Country Grounds"
        ),
        // Event 8: Blues Festival
        new EventData(
            "Blues Festival",
            Arrays.asList("Blues Band"),
            parseDate("2025-01-20"),
            "Blues House"
        ),
        // Event 9: Reggae Summer
        new EventData(
            "Reggae Summer",
            Arrays.asList("Reggae Rhythms"),
            parseDate("2025-02-25"),
            "Reggae Beach"
        ),
        // Event 10: Metal Mania
        new EventData(
            "Metal Mania",
            Arrays.asList("Metal Heads"),
            parseDate("2025-03-30"),
            "Metal Hall"
        )*/
    ));

    // Iterate through each EventData to populate the database
    for (EventData data : events) {
        // Retrieve Performers by their names using PerformerDAO
        List<Performer> performers = performerDAO.findByNames(data.performerNames);
        // Retrieve Venue by name using VenueDAO
        Venue venue = venueDAO.findByName(data.venueName);

        // Check if both performers and venue exist
        if (performers != null && !performers.isEmpty() && venue != null) {
            // Check if the Event already exists to prevent duplicates
            if (eventDAO.findByNameAndDate(data.eventName, data.eventDate) == null) {
            	 Set<Performer> performerSet = new HashSet<>(performers);
                Event event = new Event(
                		 data.eventName,
                         performerSet,
                         data.eventDate,
                         venue,
                         null// TicketCategories are handled separately
                );
                // Persist the Event to MongoDB using EventDAO
                eventDAO.create(event);
                System.out.println("Added event: " + data.eventName);
            } else {
                System.out.println("Event already exists: " + data.eventName);
            }
        } else {
            // Log if the associated Performers or Venue are not found
            System.out.println("Performers or venue not found for event: " + data.eventName);
        }
    }
    System.out.println("Events initialization completed.\n");
}

/**
 * Inner class to hold event data.
 * Encapsulates all necessary information to create an Event, including multiple performers.
 */
private static class EventData {
    String eventName;                // Name of the event
    List<String> performerNames;     // List of performer names associated with the event
    Date eventDate;                  // Date of the event
    String venueName;                // Name of the venue where the event is held

    /**
     * Constructor to initialize all fields of EventData.
     */
    EventData(String eventName, List<String> performerNames, Date eventDate, String venueName) {
        this.eventName = eventName;
        this.performerNames = performerNames;
        this.eventDate = eventDate;
        this.venueName = venueName;
    }
}

/**
 * Helper method to parse a date string into a Date object.
 */
private Date parseDate(String date) {
    try {
        return new SimpleDateFormat("yyyy-MM-dd").parse(date);
    } catch (Exception e) {
        throw new RuntimeException("Failed to parse date: " + date, e);
    }
}
}