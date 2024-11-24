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

public class TicketCategoryInitializer implements Initializer {

    private TicketCategoryDAO ticketCategoryDAO;
    private EventDAO eventDAO;

    public TicketCategoryInitializer(TicketCategoryDAO ticketCategoryDAO, EventDAO eventDAO) {
        this.ticketCategoryDAO = ticketCategoryDAO;
        this.eventDAO = eventDAO;
    }

    @Override
    public void initialize() {
        System.out.println("Initializing ticket categories...");

        List<TicketCategoryData> categories = Arrays.asList(
            // Rock Fest 2024 (Event 1) - 3 categories
            new TicketCategoryData("VIP", new BigDecimal("150.00"), 
                    parseDate("2024-05-01 00:00:00"), 
                    parseDate("2024-06-14 23:59:59"), "Front Row", 1),
            new TicketCategoryData("General Admission", new BigDecimal("75.00"), 
                    parseDate("2024-05-01 00:00:00"), 
                    parseDate("2024-06-14 23:59:59"), "Middle Section", 1),
            new TicketCategoryData("Balcony", new BigDecimal("50.00"), 
                    parseDate("2024-05-01 00:00:00"), 
                    parseDate("2024-06-14 23:59:59"), "Upper Balcony", 1),
            
            // Jazz Nights (Event 2) - 2 categories
            new TicketCategoryData("VIP", new BigDecimal("120.00"), 
                    parseDate("2024-06-01 00:00:00"), 
                    parseDate("2024-07-19 23:59:59"), "Front Stage", 2),
            new TicketCategoryData("General Admission", new BigDecimal("60.00"), 
                    parseDate("2024-06-01 00:00:00"), 
                    parseDate("2024-07-19 23:59:59"), "Main Floor", 2),
            
            // Classical Evening (Event 3) - 2 categories
            new TicketCategoryData("Premium", new BigDecimal("130.00"), 
                    parseDate("2024-07-01 00:00:00"), 
                    parseDate("2024-08-04 23:59:59"), "Orchestra", 3),
            new TicketCategoryData("Standard", new BigDecimal("70.00"), 
                    parseDate("2024-07-01 00:00:00"), 
                    parseDate("2024-08-04 23:59:59"), "Balcony", 3),
            
            // Pop Extravaganza (Event 4) - 3 categories
            new TicketCategoryData("Gold", new BigDecimal("160.00"), 
                    parseDate("2024-08-01 00:00:00"), 
                    parseDate("2024-09-09 23:59:59"), "VIP Section", 4),
            new TicketCategoryData("Silver", new BigDecimal("90.00"), 
                    parseDate("2024-08-01 00:00:00"), 
                    parseDate("2024-09-09 23:59:59"), "Main Floor", 4),
            new TicketCategoryData("Bronze", new BigDecimal("60.00"), 
                    parseDate("2024-08-01 00:00:00"), 
                    parseDate("2024-09-09 23:59:59"), "Balcony", 4),
            
            // Electronic Beats (Event 5) - 2 categories
            new TicketCategoryData("Platinum", new BigDecimal("200.00"), 
                    parseDate("2024-09-01 00:00:00"), 
                    parseDate("2024-10-24 23:59:59"), "Front Row", 5),
            new TicketCategoryData("Regular", new BigDecimal("80.00"), 
                    parseDate("2024-09-01 00:00:00"), 
                    parseDate("2024-10-24 23:59:59"), "Main Area", 5),
            
            // Hip-Hop Bash (Event 6) - 2 categories
            new TicketCategoryData("Exclusive", new BigDecimal("140.00"), 
                    parseDate("2024-10-01 00:00:00"), 
                    parseDate("2024-11-14 23:59:59"), "VIP Lounge", 6),
            new TicketCategoryData("General", new BigDecimal("65.00"), 
                    parseDate("2024-10-01 00:00:00"), 
                    parseDate("2024-11-14 23:59:59"), "General Area", 6),
            
            // Country Fair (Event 7) - 2 categories
            new TicketCategoryData("VIP", new BigDecimal("100.00"), 
                    parseDate("2024-11-01 00:00:00"), 
                    parseDate("2024-12-04 23:59:59"), "Front Stage", 7),
            new TicketCategoryData("Standard", new BigDecimal("50.00"), 
                    parseDate("2024-11-01 00:00:00"), 
                    parseDate("2024-12-04 23:59:59"), "General Area", 7),
            
            // Blues Festival (Event 8) - 2 categories
            new TicketCategoryData("VIP", new BigDecimal("130.00"), 
                    parseDate("2024-12-01 00:00:00"), 
                    parseDate("2025-01-19 23:59:59"), "VIP Section", 8),
            new TicketCategoryData("General Admission", new BigDecimal("55.00"), 
                    parseDate("2024-12-01 00:00:00"), 
                    parseDate("2025-01-19 23:59:59"), "Main Floor", 8),
            
            // Reggae Summer (Event 9) - 2 categories
            new TicketCategoryData("Gold Pass", new BigDecimal("110.00"), 
                    parseDate("2025-01-01 00:00:00"), 
                    parseDate("2025-02-24 23:59:59"), "VIP Area", 9),
            new TicketCategoryData("Regular Pass", new BigDecimal("70.00"), 
                    parseDate("2025-01-01 00:00:00"), 
                    parseDate("2025-02-24 23:59:59"), "General Area", 9),
            
            // Metal Mania (Event 10) - 2 categories
            new TicketCategoryData("VIP", new BigDecimal("140.00"), 
                    parseDate("2024-08-01 00:00:00"), 
                    parseDate("2024-09-09 23:59:59"), "Golden Circle", 10),
            new TicketCategoryData("Standard", new BigDecimal("80.00"), 
                    parseDate("2024-08-01 00:00:00"), 
                    parseDate("2024-09-09 23:59:59"), "General Area", 10)
        );

        for (TicketCategoryData data : categories) {
            Event event = eventDAO.findById(data.eventId);

            if (event == null) {
                System.out.println("Event with ID " + data.eventId + " not found for ticket category: " + data.description);
                continue;
            } else {
                System.out.println("Event found: " + event.getEventName() + " with ID: " + event.getEventId());
            }

            TicketCategory category = new TicketCategory(data.description, data.price, data.startDate, data.endDate, data.area, event);
            ticketCategoryDAO.create(category);
            System.out.println("Added ticket category: " + data.description + " for event " + event.getEventName());
        }

        System.out.println("Ticket categories initialization completed.\n");
    }

    private static class TicketCategoryData {
        String description;
        BigDecimal price;
        Date startDate;
        Date endDate;
        String area;
        int eventId;

        TicketCategoryData(String description, BigDecimal price, Date startDate, Date endDate, String area, int eventId) {
            this.description = description;
            this.price = price;
            this.startDate = startDate;
            this.endDate = endDate;
            this.area = area;
            this.eventId = eventId;
        }
    }

    private Date parseDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse date: " + date, e);
        }
    }
}