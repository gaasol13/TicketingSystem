package com.poortoys.examples.initilizer;

import com.poortoys.examples.dao.BookingTicketDAO;

import java.util.Arrays;
import java.util.List;

import com.poortoys.examples.dao.BookingDAO;
import com.poortoys.examples.dao.UserDAO;
import com.poortoys.examples.dao.TicketDAO;
import com.poortoys.examples.entities.Booking;
import com.poortoys.examples.entities.BookingTicket;
import com.poortoys.examples.entities.Ticket;
import com.poortoys.examples.entities.User;

/**
 * Initializes booking-ticket associations in the database.
 * Creates associations between existing bookings and tickets based on user and ticket information.
 */
public class BookingTicketInitializer implements Initializer {
    private final BookingTicketDAO bookingTicketDAO;
    private final BookingDAO bookingDAO;
    private final UserDAO userDAO;
    private final TicketDAO ticketDAO;

    public BookingTicketInitializer(BookingTicketDAO bookingTicketDAO, BookingDAO bookingDAO, 
                                  UserDAO userDAO, TicketDAO ticketDAO) {
        this.bookingTicketDAO = bookingTicketDAO;
        this.bookingDAO = bookingDAO;
        this.userDAO = userDAO;
        this.ticketDAO = ticketDAO;
    }

    @Override
    public void initialize() {
        System.out.println("\n=== Starting BookingTicket Initialization ===");
        
        // Debug: Print all users
        System.out.println("\nVerifying Users:");
        List<User> allUsers = userDAO.findAll();
        for (User user : allUsers) {
            System.out.println("Found User: " + user.getUserName());
        }

        // Debug: Print all tickets
        System.out.println("\nVerifying Tickets:");
        List<Ticket> allTickets = ticketDAO.findAll();
        for (Ticket ticket : allTickets) {
            System.out.println("Found Ticket: " + ticket.getSerialNumber());
        }

        // Debug: Print all bookings
        System.out.println("\nVerifying Bookings:");
        List<Booking> allBookings = bookingDAO.findAll();
        for (Booking booking : allBookings) {
            System.out.println("Found Booking: ID=" + booking.getBookingId() + 
                             ", User=" + booking.getUser().getUserName() + 
                             ", Status=" + booking.getBookingStatus());
        }

        // Now try to create the associations
        try {
            createAssociations("john_doe", Arrays.asList("RFVI0001", "RFVI0002"));
            createAssociations("jane_smith", Arrays.asList("RFGA0001"));
        } catch (Exception e) {
            System.err.println("Error during association creation: " + e.getMessage());
            e.printStackTrace();
        }

        // Verify the created associations
        System.out.println("\nVerifying Created Associations:");
        List<BookingTicket> allAssociations = bookingTicketDAO.findAll();
        System.out.println("Total associations created: " + allAssociations.size());
        for (BookingTicket bt : allAssociations) {
            System.out.println("Association: Booking=" + bt.getBooking().getBookingId() + 
                             ", Ticket=" + bt.getTicket().getSerialNumber());
        }
        
        System.out.println("\n=== BookingTicket Initialization Completed ===\n");
    }

    private void createAssociations(String username, List<String> ticketSerials) {
        System.out.println("\nCreating associations for user: " + username);
        
        // Find user
        User user = userDAO.findByUsername(username);
        if (user == null) {
            System.err.println("User not found: " + username);
            return;
        }
        System.out.println("Found user: " + user.getUserName());

        // Find booking
        Booking booking = bookingDAO.findConfirmedBookingByUser(user);
        if (booking == null) {
            System.err.println("No confirmed booking found for user: " + username);
            return;
        }
        System.out.println("Found booking: " + booking.getBookingId());

        // Create associations
        for (String serialNumber : ticketSerials) {
            try {
                Ticket ticket = ticketDAO.findBySerialNumber(serialNumber);
                if (ticket == null) {
                    System.err.println("Ticket not found: " + serialNumber);
                    continue;
                }
                System.out.println("Found ticket: " + ticket.getSerialNumber());

                // Check if association already exists
                List<BookingTicket> existingAssociations = bookingTicketDAO.findByBookingId(booking.getBookingId());
                boolean alreadyExists = existingAssociations.stream()
                    .anyMatch(bt -> bt.getTicket().getSerialNumber().equals(serialNumber));
                
                if (alreadyExists) {
                    System.out.println("Association already exists for ticket: " + serialNumber);
                    continue;
                }

                // Create new association
                BookingTicket bookingTicket = new BookingTicket(booking, ticket);
                bookingTicketDAO.create(bookingTicket);
                System.out.println("Created association for ticket: " + serialNumber);
                
            } catch (Exception e) {
                System.err.println("Error creating association for ticket " + serialNumber + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}