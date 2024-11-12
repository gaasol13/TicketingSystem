package com.poortoys.examples.initializer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;

import com.poortoys.examples.dao.EventDAO;
import com.poortoys.examples.dao.TicketCategoryDAO;
import com.poortoys.examples.dao.TicketDAO;
import com.ticketing.system.entities.Event;
import com.ticketing.system.entities.Ticket;
import com.ticketing.system.entities.TicketCategory;

public class TicketInitializer implements Initializer{

	private final TicketDAO ticketDAO;
	private final EventDAO eventDAO;
	private final TicketCategoryDAO ticketCategoryDAO;

	public TicketInitializer(TicketDAO ticketDAO, EventDAO eventDAO, TicketCategoryDAO ticketCategoryDAO) {
		this.ticketDAO = ticketDAO;
		this.eventDAO = eventDAO;
		this.ticketCategoryDAO = ticketCategoryDAO;
	}

    @Override
    public void initialize() {
        System.out.println("Initializing tickets...");

        // Retrieve all events
        List<Event> events = eventDAO.findAll();

        for (Event event : events) {
            ObjectId eventId = event.getId();

            // Retrieve ticket categories for the current event
            Set<TicketCategory> categories = ticketCategoryDAO.findByEventId(eventId);

            for (TicketCategory category : categories) {

                // Initialize seatNumber for unique serial numbers
                int seatNumber = 1;

                // Define seating configuration
                int maxRows = 5;   // Adjust as needed
                int maxSeats = 10; // Adjust as needed

                for (int row = 1; row <= maxRows; row++) {
                    for (int seat = 1; seat <= maxSeats; seat++) {

                        // Generate a unique serial number
                        String serialNumber = generateSerialNumber(event, category, seatNumber++);

                        // Check if the ticket already exists
                        if (ticketDAO.findBySerialNumber(serialNumber) == null) {
                        	// Get the price from the ticket category
                        	BigDecimal price = category.getPrice();
                            // Create new ticket
                            Ticket ticket = new Ticket(
                                    serialNumber,
                                    eventId,
                                    category.getDescription(), // Denormalized ticket category
                                    category.getArea(), // Use seating area from category
                                    "Row " + row,
                                    "Seat " + seat,
                                    "available",
                                    null,
                                    category.getPrice() // Purchase date
                            );
                            ticketDAO.create(ticket);
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
    private String generateSerialNumber(Event event, TicketCategory category, int seatNumber) {
        // Ensure the substrings are valid
        String eventCode = event.getName().length() >= 2
                ? event.getName().substring(0, 2).toUpperCase()
                : event.getName().toUpperCase();
        String categoryCode = category.getDescription().length() >= 2
                ? category.getDescription().substring(0, 2).toUpperCase()
                : category.getDescription().toUpperCase();
        return eventCode + categoryCode + String.format("%04d", seatNumber);
    }
}

