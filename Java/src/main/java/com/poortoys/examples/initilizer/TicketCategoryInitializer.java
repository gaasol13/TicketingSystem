package com.poortoys.examples.initilizer;

import com.poortoys.examples.dao.TicketCategoryDAO;
import com.poortoys.examples.dao.EventDAO;
import com.poortoys.examples.entities.TicketCategory;
import com.poortoys.examples.entities.Event;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Initializes TicketCategory entities and populates the database with sample data.
 * Links ticket categories to specific events based on predefined event IDs.
 */
public class TicketCategoryInitializer implements Initializer {

    // DAO instances for ticket categories and events
    private TicketCategoryDAO ticketCategoryDAO;
    private EventDAO eventDAO;

    /**
     * Constructor that accepts DAOs for interacting with the database.
     * @param ticketCategoryDAO DAO for TicketCategory entity.
     * @param eventDAO DAO for Event entity.
     */
    public TicketCategoryInitializer(TicketCategoryDAO ticketCategoryDAO, EventDAO eventDAO) {
        this.ticketCategoryDAO = ticketCategoryDAO;
        this.eventDAO = eventDAO;
    }

    /**
     * Initializes the database with sample ticket category data.
     * Each ticket category is associated with a predefined event ID.
     */
    @Override
    public void initialize() {
        System.out.println("Initializing ticket categories...");

        // Define ticket categories with specific event IDs for direct association
        List<TicketCategoryData> categories = Arrays.asList(
            new TicketCategoryData("VIP", new BigDecimal("150.00"), 
            		parseDate("2024-05-01 00:00:00"), 
            		parseDate("2024-06-14 23:59:59"), "Front Row", 1),
            new TicketCategoryData("General Admission", new BigDecimal("75.00"), 
            		parseDate("2024-05-01 00:00:00"), 
            		parseDate("2024-06-14 23:59:59"), "Middle Section", 1),
            new TicketCategoryData("Balcony", new BigDecimal("50.00"), 
            		parseDate("2024-05-01 00:00:00"), 
            		parseDate("2024-06-14 23:59:59"), "Upper Balcony", 1),
            // Jazz Nights Categories
            new TicketCategoryData("VIP", new BigDecimal("120.00"), 
            		parseDate("2024-06-01 00:00:00"), 
            		parseDate("2024-07-19 23:59:59"), "Front Stage", 2),
            new TicketCategoryData("General Admission", new BigDecimal("60.00"), 
            		parseDate("2024-06-01 00:00:00"), 
            		parseDate("2024-07-19 23:59:59"), "Main Floor", 2),
            // Continue for other events...
            new TicketCategoryData("VIP", new BigDecimal("130.00"), 
            		parseDate("2024-07-01 00:00:00"), 
            		parseDate("2024-08-04 23:59:59"), "Orchestra", 3),
            new TicketCategoryData("Standard", new BigDecimal("70.00"), 
            		parseDate("2024-07-01 00:00:00"), 
            		parseDate("2024-08-04 23:59:59"), "Mezzanine", 3),
            new TicketCategoryData("VIP", new BigDecimal("140.00"), 
            		parseDate("2024-08-01 00:00:00"), 
            		parseDate("2024-09-09 23:59:59"), "Golden Circle", 4),
            new TicketCategoryData("Standard", new BigDecimal("80.00"), 
            		parseDate("2024-08-01 00:00:00"), 
            		parseDate("2024-09-09 23:59:59"), "General Area", 4)
        );

        // Loop through each ticket category data item
        for (TicketCategoryData data : categories) {
            // Retrieve the Event associated with the given event ID
            Event event = eventDAO.findById(data.eventId);  // Directly find event by ID

            // Check if the event exists in the database
            if (event == null) {
                System.out.println("Event with ID " + data.eventId + " not found for ticket category: " + data.description);
                continue; // Skip this category if the event is not found
            } else {
                System.out.println("Event found: " + event.getEventName() + " with ID: " + event.getEventId());
            }

            // Create a new TicketCategory instance with the provided data
            TicketCategory category = new TicketCategory(data.description, data.price, data.startDate, data.endDate, data.area, event);

            // Persist the TicketCategory instance into the database
            ticketCategoryDAO.create(category);
            System.out.println("Added ticket category: " + data.description + " for event " + event.getEventName());
        }

        System.out.println("Ticket categories initialization completed.\n");
    }

    /**
     * Inner class to store data for initializing TicketCategory entities.
     */
    private static class TicketCategoryData {
        String description;
        BigDecimal price;
        Date startDate;
        Date endDate;
        String area;
        int eventId;  // Use eventId directly to link with Event

        /**
         * Constructor to create a TicketCategoryData object with initialization data.
         * @param description Description of the ticket category.
         * @param price Price of the ticket category.
         * @param startDate Start date for ticket availability.
         * @param endDate End date for ticket availability.
         * @param area The area/section the ticket applies to.
         * @param eventId ID of the event the ticket is associated with.
         */
        TicketCategoryData(String description, BigDecimal price, Date startDate, Date endDate, String area, int eventId) {
            this.description = description;
            this.price = price;
            this.startDate = startDate;
            this.endDate = endDate;
            this.area = area;
            this.eventId = eventId;
        }
    }

    /**
     * Parses a date string into a Date object.
     * @param date Date string in "yyyy-MM-dd HH:mm:ss" format.
     * @return Parsed Date object.
     */
    private Date parseDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse date: " + date, e);
        }
    }
}
