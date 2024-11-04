package com.poortoys.examples.initilizer;

import com.poortoys.examples.dao.VenueDAO;
import com.poortoys.examples.entities.Venue;

import java.util.Arrays;
import java.util.List;

public class VenueInitializer implements Initializer {

    private VenueDAO venueDAO;

    public VenueInitializer(VenueDAO venueDAO) {
        this.venueDAO = venueDAO;
    }

    @Override
    public void initialize() {
        System.out.println("Initializing venues...");

        // List of venues with their attributes
        List<Venue> venues = Arrays.asList(
            new Venue("Grand Arena", "123 Main St, Anytown", "Stadium", 50000),
            new Venue("Jazz Club", "456 Oak Ave, Music City", "Club", 200),
            new Venue("Symphony Hall", "789 Pine Rd, Harmony", "Concert Hall", 1500),
            new Venue("Pop Dome", "321 Maple St, Popville", "Arena", 30000),
            new Venue("Electronica Center", "654 Elm St, Tech City", "Exhibition Center", 10000),
            new Venue("Hip-Hop Venue", "987 Cedar Blvd, Beat Town", "Club", 800),
            new Venue("Country Grounds", "543 Birch Ln, Nashville", "Open Air", 25000),
            new Venue("Blues House", "210 Willow Dr, Bluesville", "Bar", 150),
            new Venue("Reggae Beach", "369 Ocean Ave, Island City", "Beach", 5000),
            new Venue("Metal Hall", "852 Steel Rd, Metal City", "Hall", 7000)
        );

        // Add each venue if it doesn't already exist
        for (Venue venue : venues) {
            if (venueDAO.findByName(venue.getVenueName()) == null) {
                venueDAO.create(venue);
                System.out.println("Added venue: " + venue.getVenueName());
            } else {
                System.out.println("Venue already exists: " + venue.getVenueName());
            }
        }
        System.out.println("Venues initialization completed.\n");
    }
}
