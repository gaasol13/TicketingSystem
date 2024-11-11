package com.poortoys.examples.initializer;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.poortoys.examples.dao.EventDAO;
import com.poortoys.examples.dao.TicketCategoryDAO;
import com.ticketing.system.entities.Event;
import com.ticketing.system.entities.TicketCategory;

public class TicketCategoryInitializer implements Initializer {

    // DAO instances for ticket categories and events
    private final TicketCategoryDAO ticketCategoryDAO;
    private final EventDAO eventDAO;

    /**
     * Constructor that accepts DAOs for interacting with the database.
     */
    public TicketCategoryInitializer(TicketCategoryDAO ticketCategoryDAO, EventDAO eventDAO) {
        this.ticketCategoryDAO = ticketCategoryDAO;
        this.eventDAO = eventDAO;
    }

    /**
     * Initializes the database with sample ticket category data.
     * Each ticket category is associated with a specific event based on event name and date.
     */
    @Override
    public void initialize() {
        System.out.println("Initializing ticket categories...");

        // Define ticket categories with specific event names and dates for direct association
        List<TicketCategoryData> categories = Arrays.asList(
            // Ticket categories for "Rock Fest 2024"
            new TicketCategoryData("VIP", new BigDecimal("150.00"), 
                    parseDateTime("2024-05-01 00:00:00"), 
                    parseDateTime("2024-06-14 23:59:59"), "Front Row", "Rock Fest 2024", parseDate("2024-06-15")),
            new TicketCategoryData("General Admission", new BigDecimal("75.00"), 
                    parseDateTime("2024-05-01 00:00:00"), 
                    parseDateTime("2024-06-14 23:59:59"), "Middle Section", "Rock Fest 2024", parseDate("2024-06-15")),
            new TicketCategoryData("Balcony", new BigDecimal("50.00"), 
                    parseDateTime("2024-05-01 00:00:00"), 
                    parseDateTime("2024-06-14 23:59:59"), "Upper Balcony", "Rock Fest 2024", parseDate("2024-06-15")),
            
            // Ticket categories for "Jazz Nights"
            new TicketCategoryData("VIP", new BigDecimal("120.00"), 
                    parseDateTime("2024-06-01 00:00:00"), 
                    parseDateTime("2024-07-19 23:59:59"), "Front Stage", "Jazz Nights", parseDate("2024-07-20")),
            new TicketCategoryData("General Admission", new BigDecimal("60.00"), 
                    parseDateTime("2024-06-01 00:00:00"), 
                    parseDateTime("2024-07-19 23:59:59"), "Main Floor", "Jazz Nights", parseDate("2024-07-20")),
            
            // Ticket categories for "Classical Evening"
            new TicketCategoryData("Premium", new BigDecimal("130.00"), 
                    parseDateTime("2024-07-01 00:00:00"), 
                    parseDateTime("2024-08-04 23:59:59"), "Orchestra", "Classical Evening", parseDate("2024-08-05")),
            new TicketCategoryData("Standard", new BigDecimal("70.00"), 
                    parseDateTime("2024-07-01 00:00:00"), 
                    parseDateTime("2024-08-04 23:59:59"), "Balcony", "Classical Evening", parseDate("2024-08-05")),
            
            // Ticket categories for "Pop Extravaganza"
            new TicketCategoryData("Gold", new BigDecimal("160.00"), 
                    parseDateTime("2024-08-01 00:00:00"), 
                    parseDateTime("2024-09-09 23:59:59"), "VIP Section", "Pop Extravaganza", parseDate("2024-09-10")),
            new TicketCategoryData("Silver", new BigDecimal("90.00"), 
                    parseDateTime("2024-08-01 00:00:00"), 
                    parseDateTime("2024-09-09 23:59:59"), "Main Floor", "Pop Extravaganza", parseDate("2024-09-10")),
            new TicketCategoryData("Bronze", new BigDecimal("60.00"), 
                    parseDateTime("2024-08-01 00:00:00"), 
                    parseDateTime("2024-09-09 23:59:59"), "Balcony", "Pop Extravaganza", parseDate("2024-09-10")),
            
            // Ticket categories for "Electronic Beats"
            new TicketCategoryData("Platinum", new BigDecimal("200.00"), 
                    parseDateTime("2024-09-01 00:00:00"), 
                    parseDateTime("2024-10-24 23:59:59"), "Front Row", "Electronic Beats", parseDate("2024-10-25")),
            new TicketCategoryData("Regular", new BigDecimal("80.00"), 
                    parseDateTime("2024-09-01 00:00:00"), 
                    parseDateTime("2024-10-24 23:59:59"), "Main Area", "Electronic Beats", parseDate("2024-10-25")),
            
            // Ticket categories for "Hip-Hop Bash"
            new TicketCategoryData("Exclusive", new BigDecimal("140.00"), 
                    parseDateTime("2024-10-01 00:00:00"), 
                    parseDateTime("2024-11-14 23:59:59"), "VIP Lounge", "Hip-Hop Bash", parseDate("2024-11-15")),
            new TicketCategoryData("General", new BigDecimal("65.00"), 
                    parseDateTime("2024-10-01 00:00:00"), 
                    parseDateTime("2024-11-14 23:59:59"), "General Area", "Hip-Hop Bash", parseDate("2024-11-15")),
            
            // Ticket categories for "Country Fair"
            new TicketCategoryData("VIP", new BigDecimal("100.00"), 
                    parseDateTime("2024-11-01 00:00:00"), 
                    parseDateTime("2024-12-04 23:59:59"), "Front Stage", "Country Fair", parseDate("2024-12-05")),
            new TicketCategoryData("Standard", new BigDecimal("50.00"), 
                    parseDateTime("2024-11-01 00:00:00"), 
                    parseDateTime("2024-12-04 23:59:59"), "General Area", "Country Fair", parseDate("2024-12-05")),
            
            // Ticket categories for "Blues Festival"
            new TicketCategoryData("VIP", new BigDecimal("130.00"), 
                    parseDateTime("2024-12-01 00:00:00"), 
                    parseDateTime("2025-01-19 23:59:59"), "VIP Section", "Blues Festival", parseDate("2025-01-20")),
            new TicketCategoryData("General Admission", new BigDecimal("55.00"), 
                    parseDateTime("2024-12-01 00:00:00"), 
                    parseDateTime("2025-01-19 23:59:59"), "Main Floor", "Blues Festival", parseDate("2025-01-20")),
            
            // Ticket categories for "Reggae Summer"
            new TicketCategoryData("Gold Pass", new BigDecimal("110.00"), 
                    parseDateTime("2025-01-01 00:00:00"), 
                    parseDateTime("2025-02-24 23:59:59"), "VIP Area", "Reggae Summer", parseDate("2025-02-25")),
            new TicketCategoryData("Regular Pass", new BigDecimal("70.00"), 
                    parseDateTime("2025-01-01 00:00:00"), 
                    parseDateTime("2025-02-24 23:59:59"), "General Area", "Reggae Summer", parseDate("2025-02-25")),
            
            // Ticket categories for "Metal Mania"
            new TicketCategoryData("VIP", new BigDecimal("140.00"), 
                    parseDateTime("2024-08-01 00:00:00"), 
                    parseDateTime("2024-09-09 23:59:59"), "Golden Circle", "Metal Mania", parseDate("2025-03-30")),
            new TicketCategoryData("Standard", new BigDecimal("80.00"), 
                    parseDateTime("2024-08-01 00:00:00"), 
                    parseDateTime("2024-09-09 23:59:59"), "General Area", "Metal Mania", parseDate("2025-03-30"))
        );

        // Iterate through each TicketCategoryData to populate the database
        for (TicketCategoryData data : categories) {
            // Retrieve the Event by its name and date
            Event event = eventDAO.findByNameAndDate(data.eventName, data.eventDate);

            // Check if the event exists
            if (event == null) {
                System.out.println("Event not found for ticket category: " + data.description + " (Event: " + data.eventName + ")");
                continue; // Skip this category if the event is not found
            } else {
                System.out.println("Event found: " + event.getName() + " on " + event.getDate());
            }

            // Create a new TicketCategory instance with the provided data
            TicketCategory category = new TicketCategory(data.description, data.price, data.startDate, data.endDate, data.area);

            // Add the TicketCategory to the Event's list of ticket categories
            ticketCategoryDAO.addTicketCategory(event.getId(), category);
            System.out.println("Added ticket category: " + data.description + " for event " + event.getName());
        }

        System.out.println("Ticket categories initialization completed.\n");
    }

    /**
     * Inner class to store data for initializing TicketCategory entities.
     */
    private static class TicketCategoryData {
        String description;      // Description of the ticket category
        BigDecimal price;        // Price of the ticket
        Date startDate;          // Start date for ticket availability
        Date endDate;            // End date for ticket availability
        String area;             // Seating area description
        String eventName;        // Name of the event to associate with
        Date eventDate;          // Date of the event to ensure correct association

        /**
         * Constructor to create a TicketCategoryData object with initialization data.
         */
        TicketCategoryData(String description, BigDecimal price, Date startDate, Date endDate, String area, String eventName, Date eventDate) {
            this.description = description;
            this.price = price;
            this.startDate = startDate;
            this.endDate = endDate;
            this.area = area;
            this.eventName = eventName;
            this.eventDate = eventDate;
        }
    }

    /**
     * Parses a date string in "yyyy-MM-dd" format.
     */
    private Date parseDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse date: " + date, e);
        }
    }

    /**
     * Parses a datetime string in "yyyy-MM-dd HH:mm:ss" format.
     */
    private Date parseDateTime(String datetime) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(datetime);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse datetime: " + datetime, e);
        }
    }
}