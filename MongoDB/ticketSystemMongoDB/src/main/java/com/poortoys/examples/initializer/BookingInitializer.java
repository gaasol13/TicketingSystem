package com.poortoys.examples.initializer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;

import com.poortoys.examples.dao.BookingDAO;
import com.poortoys.examples.dao.EventDAO;
import com.poortoys.examples.dao.TicketDAO;
import com.poortoys.examples.dao.UserDAO;
import com.ticketing.system.entities.Booking;
import com.ticketing.system.entities.Event;
import com.ticketing.system.entities.Ticket;
import com.ticketing.system.entities.TicketCategory;
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

        // Specify the event ID for "Jazz Nights"
        ObjectId eventId = new ObjectId("673133acaa85ed04a55c969d"); // Ensure this ID is correct and exists

        // Retrieve the event
        Event event = eventDAO.findById(eventId);
        if (event == null) {
            System.out.println("Event 'Jazz Nights' not found.");
            return;
        }

        // Create a map of TicketCategory description to price for easy lookup
        Map<String, BigDecimal> categoryPriceMap = event.getTicketCategories().stream()
                .collect(Collectors.toMap(
                        TicketCategory::getDescription,
                        TicketCategory::getPrice
                ));
        
        // Check if categoryPriceMap is populated
        if (categoryPriceMap.isEmpty()) {
            System.out.println("No TicketCategories with valid prices found for event: " + eventId);
            return;
        }

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
            List<Ticket> availableTickets = ticketDAO.findAvailableTickets(eventId);

            if (availableTickets.isEmpty()) {
                System.out.println("No available tickets for user: " + userName);
                continue;
            }

            // Limit the number of tickets to book based on remaining bookings allowed
            int ticketsToBookCount = Math.min(remainingBookingsAllowed, 2); // Assuming we want to book up to 2 tickets

            // Ensure there are enough available tickets
            if (availableTickets.size() < ticketsToBookCount) {
                ticketsToBookCount = availableTickets.size();
            }

            // Select tickets to book
            List<Ticket> ticketsToBook = availableTickets.subList(0, ticketsToBookCount);
            List<ObjectId> ticketIds = new ArrayList<>();

            // Update tickets to 'booked' status and collect their IDs
            for (Ticket ticket : ticketsToBook) {
                String category = ticket.getTicketCategory();
                BigDecimal price = categoryPriceMap.get(category);

                if (price == null) {
                    System.out.println("Ticket category '" + category + "' not found in TicketCategory map. Skipping ticket ID: " + ticket.getId());
                    continue; // Skip if the category price is not found
                }

                ticket.setStatus("booked");
                ticket.setPurchaseDate(new Date());
                ticketDAO.update(ticket);
                ticketIds.add(ticket.getId());

                System.out.println("Booked Ticket ID: " + ticket.getId() + ", Category: " + category + ", Price: " + price);
            }

            if (ticketIds.isEmpty()) {
                System.out.println("No valid tickets available to book for user: " + userName);
                continue;
            }

            // Calculate total price based on TicketCategory prices
            BigDecimal totalPrice = ticketsToBook.stream()
                    .filter(ticket -> categoryPriceMap.containsKey(ticket.getTicketCategory()))
                    .map(ticket -> categoryPriceMap.get(ticket.getTicketCategory()))
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
                    "booked",
                    ticketIds
            );

            // Save the booking
            bookingDAO.create(booking);
            System.out.println("Booking created for user: " + userName + " with total price: " + totalPrice);
        }

        System.out.println("Bookings initialization completed.");
    }
}