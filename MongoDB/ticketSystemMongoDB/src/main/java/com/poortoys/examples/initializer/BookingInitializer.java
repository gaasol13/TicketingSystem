package com.poortoys.examples.initializer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.poortoys.examples.dao.BookingDAO;
import com.poortoys.examples.dao.EventDAO;
import com.poortoys.examples.dao.TicketDAO;
import com.poortoys.examples.dao.UserDAO;
import com.ticketing.system.entities.Booking;
import com.ticketing.system.entities.Ticket;
import com.ticketing.system.entities.User;

public class BookingInitializer implements Initializer{
	
	private BookingDAO bookingDAO;
	private UserDAO userDAO;
    private final TicketDAO ticketDAO;
    private final EventDAO eventDAO;
	
	
	public BookingInitializer(BookingDAO bookingDAO, UserDAO userDAO, TicketDAO ticketDAO, EventDAO eventDAO) {
        this.bookingDAO = bookingDAO;
        this.userDAO = userDAO;
        this.ticketDAO = ticketDAO;
        this.eventDAO = eventDAO;
    }
	
	@Override
	public void initialize() {
		System.out.println("Initializing bookings for 'Jazz Nights'...");
		
		// Sample user data
        List<String> userNames = List.of("john_doe", "jane_smith");

        // Assume we have an event ID
        ObjectId eventId = 	new ObjectId("673133acaa85ed04a55c969d"); 	/* Specify or retrieve the event ID */;

        for (String userName : userNames) {
            // Retrieve the user
            User user = userDAO.findByUserName(userName);
            if (user == null) {
                System.out.println("User not found: " + userName);
                continue;
            }

            // Check if user has reached the booking limit for the event
            long existingBookingsCount = bookingDAO.countBookingsByUserAndEvent(user.getId(), eventId);
            if (existingBookingsCount >= 5) {
                System.out.println("User " + userName + " has already reached the booking limit for the event.");
                continue;
            }

            // Determine how many more bookings the user can make
            int remainingBookingsAllowed = (int)(5 - existingBookingsCount);

            // Retrieve available tickets for the event
            List<Ticket> availableTickets = ticketDAO.findAvailableTicketsByEventId(eventId);

            if (availableTickets.isEmpty()) {
                System.out.println("No available tickets for user: " + userName);
                continue;
            }

            // Limit the number of tickets to book based on remaining bookings allowed
            int ticketsToBookCount = Math.min(remainingBookingsAllowed, 2); // Assuming we want to book 2 tickets

            // Select tickets to book
            List<Ticket> ticketsToBook = availableTickets.subList(0, ticketsToBookCount);
            List<ObjectId> ticketIds = new ArrayList<>();

            // Update tickets to 'booked' status and collect their IDs
            for (Ticket ticket : ticketsToBook) {
                ticket.setStatus("booked");
                ticket.setPurchaseDate(new Date());
                ticketDAO.update(ticket);
                ticketIds.add(ticket.getId());
            }

            // Calculate total price
            BigDecimal totalPrice = ticketsToBook.stream()
                    .map(Ticket::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Create the booking
            Booking booking = new Booking(
                    user.getId(),
                    eventId,
                    user.getEmail(),
                    new Date(), // deliveryTime
                    new Date(), // timePaid
                    new Date(), // timeSent
                    totalPrice,
                    BigDecimal.ZERO, // discount
                    totalPrice, // finalPrice (totalPrice - discount)
                    "confirmed",
                    ticketIds
            );

            // Save the booking
            bookingDAO.create(booking);
            System.out.println("Booking created for user: " + userName);
        }

        System.out.println("Bookings initialization completed.");
    }
}


