package com.ticketing.system.simulation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.bson.types.ObjectId;
import com.mongodb.client.ClientSession;
import com.poortoys.examples.dao.BookingDAO;
import com.poortoys.examples.dao.EventDAO;
import com.poortoys.examples.dao.TicketDAO;
import com.poortoys.examples.dao.UserDAO;
import com.ticketing.system.entities.Booking;
import com.ticketing.system.entities.Event;
import com.ticketing.system.entities.Ticket;
import com.ticketing.system.entities.User;

import dev.morphia.Datastore;

/**
 * Service class handling ticket booking operations using MongoDB.
 * Demonstrates MongoDB's approach to:
 * 1. Document-based transactions
 * 2. Schema flexibility
 * 3. Atomic operations for concurrency
 */
public class BookingService {
    private final BookingDAO bookingDAO;
    private final TicketDAO ticketDAO;
    private final UserDAO userDAO;
    private final EventDAO eventDAO;
    private final Datastore datastore;
    
    // Thread-safe counters for monitoring concurrent operations
    private AtomicInteger successfulBookings = new AtomicInteger(0);
    private AtomicInteger failedBookings = new AtomicInteger(0);

    public BookingService(BookingDAO bookingDAO, TicketDAO ticketDAO, 
            UserDAO userDAO, EventDAO eventDAO, Datastore datastore) {
        this.bookingDAO = bookingDAO;
        this.ticketDAO = ticketDAO;
        this.userDAO = userDAO;
        this.eventDAO = eventDAO;
        this.datastore = datastore;
    }

    /**
     * Main booking method demonstrating MongoDB's transaction handling
     * Shows how MongoDB manages concurrent access through:
     * - Client sessions for transactions
     * - Document-level atomicity
     * - Eventual consistency model
     */
    public boolean bookTickets(ObjectId userId, ObjectId eventId, int quantity) {
        System.out.println("User " + userId + " attempting to book " + quantity + " tickets");
        
        try (ClientSession session = datastore.startSession()) {
            session.startTransaction();
            try {
                // 1. Match MySQL's user validation first
                User user = userDAO.findById(userId);
                if (user == null) {
                    System.out.println("User not found: " + userId);
                    failedBookings.incrementAndGet();
                    session.abortTransaction();
                    return false;
                }
                
             // Get event for price calculation
                Event event = eventDAO.findById(eventId);
                if (event == null) {
                    failedBookings.incrementAndGet();
                    session.abortTransaction();
                    return false;
                }

                // 2. Get available tickets first like MySQL
                long availableCount  = ticketDAO.countAvailableTickets(eventId);
                if (availableCount  < quantity) {
                    System.out.println("Not enough tickets available");
                    failedBookings.incrementAndGet();
                    session.abortTransaction();
                    return false;
                }

                // 3. Lock tickets individually like MySQL
                List<Ticket> availableTickets = ticketDAO.findAvailableTicketsByEventId(eventId);
                List<Ticket> lockedTickets = new ArrayList<>();
                
                for (int i = 0; i < quantity; i++) {
                    Ticket ticket = availableTickets.get(i);
                    Ticket lockedTicket = ticketDAO.findAndModifyTicket(
                        session, 
                        ticket.getId(), 
                        "LOCKED"
                    );
                    if (lockedTicket == null) {
                        System.out.println("Failed to lock ticket: " + ticket.getId());
                        session.abortTransaction();
                        return false;
                    }
                    lockedTickets.add(lockedTicket);
                }

                // 4. Create booking with locked tickets
                BigDecimal totalPrice = calculateTotalPrice(event, lockedTickets);
                Booking booking = new Booking(
                    userId,
                    eventId,
                    user.getEmail(), // Use actual email like MySQL
                    new Date(),
                    new Date(),
                    new Date(),
                    totalPrice,
                    calculateDiscount(user, totalPrice), // Match MySQL's discount
                    totalPrice,
                    "CONFIRMED",
                    extractTicketIds(lockedTickets)
                );

                // 5. Save booking and update tickets atomically
                bookingDAO.create(session, booking);
                for (Ticket ticket : lockedTickets) {
                    ticketDAO.update(session, ticket.getId(), "SOLD");
                }

                session.commitTransaction();
                successfulBookings.incrementAndGet();
                return true;

            } catch (Exception e) {
                System.err.println("Booking failed: " + e.getMessage());
                session.abortTransaction();
                failedBookings.incrementAndGet();
                return false;
            }
        }
    }

 

    /**
     * Retrieves price from embedded ticket categories document
     */
    private BigDecimal getPriceForCategory(Event event, String ticketCategory) {
        return event.getTicketCategories().stream()
                .filter(tc -> tc.getDescription().equalsIgnoreCase(ticketCategory))
                .map(tc -> tc.getPrice())
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }
    private BigDecimal calculateDiscount(User user, BigDecimal totalPrice) {
        long userBookingsCount = bookingDAO.countBookingsByUser(user.getId());
        if (userBookingsCount > 5) {
            return totalPrice.multiply(new BigDecimal("0.05")); // 5% discount
        }
        return BigDecimal.ZERO;
    }

    
    /**
     * Calculates total price demonstrating MongoDB's handling of embedded documents
     */
    private BigDecimal calculateTotalPrice(Event event, List<Ticket> bookedTickets) {
        BigDecimal total = BigDecimal.ZERO;
        for (Ticket ticket : bookedTickets) {
            BigDecimal price = getPriceForCategory(event, ticket.getTicketCategory());
            total = total.add(price);
        }
        return total;
    }

    private String getUserEmail(ObjectId userId) {
        User user = userDAO.findById(userId);
        return user != null ? user.getEmail() : "unknown@example.com";
    }

    private List<ObjectId> extractTicketIds(List<Ticket> tickets) {
        List<ObjectId> ticketIds = new ArrayList<>();
        for (Ticket ticket : tickets) {
            ticketIds.add(ticket.getId());
        }
        return ticketIds;
    }

    public int getSuccessfulBookings() {
        return successfulBookings.get();
    }

    public int getFailedBookings() {
        return failedBookings.get();
    }
}