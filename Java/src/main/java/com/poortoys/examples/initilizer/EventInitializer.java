package com.poortoys.examples.initilizer;

import com.poortoys.examples.dao.EventDAO;
import com.poortoys.examples.dao.PerformerDAO;
import com.poortoys.examples.dao.VenueDAO;
import com.poortoys.examples.entities.Event;
import com.poortoys.examples.entities.Performer;
import com.poortoys.examples.entities.Venue;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EventInitializer implements Initializer {

    private EventDAO eventDAO;
    private PerformerDAO performerDAO;
    private VenueDAO venueDAO;

    public EventInitializer(EventDAO eventDAO, PerformerDAO performerDAO, VenueDAO venueDAO) {
        this.eventDAO = eventDAO;
        this.performerDAO = performerDAO;
        this.venueDAO = venueDAO;
    }

    @Override
    public void initialize() {
        System.out.println("Initializing events...");

        // List of events with details
        List<EventData> events = Arrays.asList(
            new EventData("Rock Fest 2024", "The Rockers", createDate(2024, 6, 15), "Grand Arena"),
            new EventData("Jazz Nights", "Jazz Masters", createDate(2024, 7, 20), "Jazz Club"),
            new EventData("Classical Evening", "Classical Quartet", createDate(2024, 8, 5), "Symphony Hall")
/*            new EventData("Pop Extravaganza", "Pop Icons", createDate(2024, 9, 10), "Pop Dome"),
            new EventData("Electronic Beats", "Electronica", createDate(2024, 10, 25), "Electronica Center"),
            new EventData("Hip-Hop Bash", "Hip-Hop Crew", createDate(2024, 11, 15), "Hip-Hop Venue"),
            new EventData("Country Fair", "Country Stars", createDate(2024, 12, 5), "Country Grounds"),
            new EventData("Blues Festival", "Blues Band", createDate(2025, 1, 20), "Blues House"),
            new EventData("Reggae Summer", "Reggae Rhythms", createDate(2025, 2, 25), "Reggae Beach"),
            new EventData("Metal Mania", "Metal Heads", createDate(2025, 3, 30), "Metal Hall")*/
        );

        for (EventData eventData : events) {
            Performer performer = performerDAO.findByName(eventData.performerName);
            Venue venue = venueDAO.findByName(eventData.venueName);

            if (performer != null && venue != null) {
                if (eventDAO.findByNameAndDate(eventData.eventName, eventData.eventDate) == null) {
                    Event event = new Event(eventData.eventName, performer, eventData.eventDate, venue);
                    eventDAO.create(event);
                    System.out.println("Added event: " + eventData.eventName);
                } else {
                    System.out.println("Event already exists: " + eventData.eventName);
                }
            } else {
                System.out.println("Performer or venue not found for event: " + eventData.eventName);
            }
        }
        System.out.println("Events initialization completed.\n");
    }

    private static class EventData {
        String eventName;
        String performerName;
        Date eventDate;
        String venueName;

        EventData(String eventName, String performerName, Date eventDate, String venueName) {
            this.eventName = eventName;
            this.performerName = performerName;
            this.eventDate = eventDate;
            this.venueName = venueName;
        }
    }

    // Helper method to create Date objects
    private Date createDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day); // Calendar months are 0-based
        return calendar.getTime();
    }
}
