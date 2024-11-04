package com.poortoys.examples.initilizer;

import com.poortoys.examples.dao.TicketDAO;
import com.poortoys.examples.dao.EventDAO;
import com.poortoys.examples.dao.TicketCategoryDAO;
import com.poortoys.examples.entities.Ticket;
import com.poortoys.examples.entities.Event;
import com.poortoys.examples.entities.TicketCategory;
import com.poortoys.examples.entities.TicketStatus;

import java.util.Date;
import java.util.List;

public class TicketInitializer implements Initializer {

    private TicketDAO ticketDAO;
    private EventDAO eventDAO;
    private TicketCategoryDAO ticketCategoryDAO;

    public TicketInitializer(TicketDAO ticketDAO, EventDAO eventDAO, TicketCategoryDAO ticketCategoryDAO) {
        this.ticketDAO = ticketDAO;
        this.eventDAO = eventDAO;
        this.ticketCategoryDAO = ticketCategoryDAO;
    }

    @Override
    public void initialize() {
        System.out.println("Initializing tickets...");

        List<Event> events = eventDAO.findAll();

        // Loop through each event and create tickets
        for (Event event : events) {
            // Fetch ticket categories for this event
            List<TicketCategory> categories = ticketCategoryDAO.findByEventId(event.getEventId());

            int seatNumber = 1; // Example seat numbering

            for (TicketCategory category : categories) {
                for (int row = 1; row <= 5; row++) { // Example rows
                    for (int seat = 1; seat <= 10; seat++) { // Example seats per row
                        String serialNumber = event.getEventName().substring(0, 2).toUpperCase() + 
                                              category.getDescription().substring(0, 2).toUpperCase() + 
                                              String.format("%04d", seatNumber++);
                        
                        Ticket ticket = new Ticket(serialNumber, event, category, 
                                                   "Section A", 
                                                   "Row " + row, 
                                                   "Seat " + seat, 
                                                   TicketStatus.AVAILABLE);
                        ticketDAO.create(ticket);
                        System.out.println("Added ticket: " + serialNumber);
                    }
                }
            }
        }

        System.out.println("Tickets initialization completed.");
    }
}
