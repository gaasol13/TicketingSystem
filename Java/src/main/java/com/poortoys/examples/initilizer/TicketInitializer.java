package com.poortoys.examples.initilizer;

import com.poortoys.examples.dao.TicketDAO;
import com.poortoys.examples.dao.EventDAO;
import com.poortoys.examples.dao.TicketCategoryDAO;
import com.poortoys.examples.entities.Ticket;
import com.poortoys.examples.entities.Event;
import com.poortoys.examples.entities.TicketCategory;
import com.poortoys.examples.entities.TicketStatus;

import java.util.List;

/**
 * Initializes the database with ticket data for each event and ticket category.
 * Prevents duplicates by checking for existing tickets based on serial numbers.
 */
public class TicketInitializer implements Initializer {

    // DAO instances for database interaction
    private final TicketDAO ticketDAO;
    private final EventDAO eventDAO;
    private final TicketCategoryDAO ticketCategoryDAO;

    /**
     * Constructor for TicketInitializer.
     *
     * @param ticketDAO         Data Access Object for Ticket entity
     * @param eventDAO          Data Access Object for Event entity
     * @param ticketCategoryDAO Data Access Object for TicketCategory entity
     */
    public TicketInitializer(TicketDAO ticketDAO, EventDAO eventDAO, TicketCategoryDAO ticketCategoryDAO) {
        this.ticketDAO = ticketDAO;
        this.eventDAO = eventDAO;
        this.ticketCategoryDAO = ticketCategoryDAO;
    }

    /**
     * Initializes tickets for each event and ticket category.
     * Checks for duplicate serial numbers to prevent constraint violations.
     */
    @Override
    public void initialize() {
        System.out.println("Initializing tickets...");

        // Loop through each event
        for (Event event : eventDAO.findAll()) {

            // Retrieve categories for the current event by event ID
            List<TicketCategory> categories = ticketCategoryDAO.findByEventId(event.getEventId());

            for (TicketCategory category : categories) {
                
                // Initialize seatNumber to reset for each new category
                int seatNumber = 1; // Resetting seatNumber for each category

                //create tickets for each row and seat up to a maximum limit to avoid infinite generation
                int maxRows = 50;   // Maximum rows per category, can be adjusted
                int maxSeats = 200; // Maximum seats per row, can be adjusted

                for (int row = 1; row <= maxRows; row++) {
                    for (int seat = 1; seat <= maxSeats; seat++) {

                        //Generate a unique serial number for each ticket
                        String serialNumber = generateSerialNumber(event, category, seatNumber++);

                        //check if the ticket with this serial number already exists
                        if (ticketDAO.findBySerialNumber(serialNumber) == null) {
                            // If not, create and save a new Ticket object
                            Ticket ticket = new Ticket(
                                    serialNumber,
                                    event,
                                    category,
                                    "Section A",
                                    "Row " + row,
                                    "Seat " + seat,
                                    TicketStatus.AVAILABLE
                            );
                            ticketDAO.create(ticket);  // Persist ticket to the database
                            System.out.println("Added ticket: " + serialNumber);
                        } else {
                            System.out.println("Ticket with serial number " + serialNumber + " already exists, skipping.");
                        }
                    }
                }
            }
        }
        System.out.println("Tickets initialization completed.");
    }

    /**
     * Generates a serial number for a ticket based on event name, category, and seat number.
     * Ensures serial number is unique for each ticket created.
     *
     * @param event          The event for which the ticket is created
     * @param ticketCategory The category of the ticket
     * @param seatNumber     The seat number to include in the serial number
     * @return A unique serial number for the ticket
     */
    private String generateSerialNumber(Event event, TicketCategory ticketCategory, int seatNumber) {
        // Use event and category name initials with seat number to create unique identifier
        return event.getEventName().substring(0, 2).toUpperCase() + 
               ticketCategory.getDescription().substring(0, 2).toUpperCase() + 
               String.format("%04d", seatNumber);
    }
}
