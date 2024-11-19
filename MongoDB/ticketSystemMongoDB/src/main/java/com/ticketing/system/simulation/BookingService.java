package com.ticketing.system.simulation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
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
    private AtomicInteger concurrencyConflicts = new AtomicInteger(0);
    private AtomicInteger dynamicFieldUpdates = new AtomicInteger(0);

    public BookingService(BookingDAO bookingDAO, TicketDAO ticketDAO, 
            UserDAO userDAO, EventDAO eventDAO, Datastore datastore) {
        this.bookingDAO = bookingDAO;
        this.ticketDAO = ticketDAO;
        this.userDAO = userDAO;
        this.eventDAO = eventDAO;
        this.datastore = datastore;
    }
    
    
    /**
     * POSITIVE SCENARIO: Demonstrates MongoDB's flexible document structure
     * by handling dynamic ticket categories and pricing
     */
    public boolean createDynamicTicket(ObjectId eventId, Map<String, Object> dynamicFields) {
        try {
            long startTime = System.currentTimeMillis();
            
            // Create a base ticket document
            Document ticketDoc = new Document()
                .append("eventId", eventId)
                .append("status", "AVAILABLE")
                .append("created", new Date());
            
            // Dynamically add custom fields (MongoDB's schema flexibility)
            dynamicFields.forEach((key, value) -> {
                ticketDoc.append(key, value);
                dynamicFieldUpdates.incrementAndGet();
            });
            
            // Insert the dynamic ticket document without using session
            MongoCollection<Document> collection = datastore.getDatabase().getCollection("tickets");
            collection.insertOne(ticketDoc);
            
            System.out.println("Successfully created dynamic ticket with " + 
                dynamicFields.size() + " custom fields");
            System.out.println("Operation time: " + (System.currentTimeMillis() - startTime) + "ms");
            return true;
        } catch (Exception e) {
            System.err.println("Failed to create dynamic ticket: " + e.getMessage());
            return false;
        }
    }

    /**
     * NEGATIVE SCENARIO: Shows MongoDB's concurrency challenges during ticket booking
     * - Client sessions for transactions
     * - Document-level atomicity
     * - Eventual consistency model
     */
    public boolean bookTickets(ObjectId userId, ObjectId eventId, int quantity) {
        long startTime = System.currentTimeMillis();
        System.out.println("User " + userId + " attempting to book " + quantity + " tickets");
        
        try (ClientSession session = datastore.startSession()) {
            session.startTransaction();
            try {
                // 1. Validate user and check ticket availability
                User user = userDAO.findById(userId);
                if (user == null) {
                    recordFailure("User not found", startTime);
                    return false;
                }

                Event event = eventDAO.findById(eventId);
                if (event == null) {
                    recordFailure("Event not found", startTime);
                    return false;
                }

                // 2. Check ticket availability (potential race condition point)
                List<Ticket> availableTickets = ticketDAO.findAvailableTicketsByEventId(eventId);
                if (availableTickets.size() < quantity) {
                    recordFailure("Insufficient tickets", startTime);
                    return false;
                }

                // 3. Attempt to lock tickets (concurrency challenge demonstration)
                List<Ticket> lockedTickets = new ArrayList<>();
                for (int i = 0; i < quantity; i++) {
                    try {
                        Ticket ticket = availableTickets.get(i);
                        Ticket lockedTicket = ticketDAO.findAndModifyTicket(session, ticket.getId(), "LOCKED");
                        if (lockedTicket == null) {
                            concurrencyConflicts.incrementAndGet();
                            throw new RuntimeException("Concurrent access detected for ticket: " + ticket.getId());
                        }
                        lockedTickets.add(lockedTicket);
                    } catch (Exception e) {
                        session.abortTransaction();
                        recordFailure("Concurrent access conflict", startTime);
                        return false;
                    }
                }

                // 4. Create booking if locks were successful
                BigDecimal totalPrice = calculateTotalPrice(event, lockedTickets);
                Booking booking = createBooking(user, event, lockedTickets, totalPrice);
                bookingDAO.create(session, booking);

                // 5. Update ticket status (another potential consistency challenge)
                for (Ticket ticket : lockedTickets) {
                    ticketDAO.update(session, ticket.getId(), "SOLD");
                }

                session.commitTransaction();
                recordSuccess("Booking completed successfully", startTime);
                return true;

            } catch (Exception e) {
                session.abortTransaction();
                recordFailure("Transaction failed: " + e.getMessage(), startTime);
                return false;
            }
        }
    }

    private void recordSuccess(String message, long startTime) {
        successfulBookings.incrementAndGet();
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("✓ " + message + " (Duration: " + duration + "ms)");
    }

    private void recordFailure(String reason, long startTime) {
        failedBookings.incrementAndGet();
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("✗ Failed: " + reason + " (Duration: " + duration + "ms)");
    }

    // Helper methods remain the same...
    private BigDecimal calculateTotalPrice(Event event, List<Ticket> tickets) {
        return tickets.stream()
            .map(ticket -> getPriceForCategory(event, ticket.getTicketCategory()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getPriceForCategory(Event event, String category) {
        return event.getTicketCategories().stream()
            .filter(tc -> tc.getDescription().equalsIgnoreCase(category))
            .map(tc -> tc.getPrice())
            .findFirst()
            .orElse(BigDecimal.ZERO);
    }

    private Booking createBooking(User user, Event event, List<Ticket> tickets, BigDecimal totalPrice) {
        BigDecimal discount = calculateDiscount(user, totalPrice);
        return new Booking(
            user.getId(),
            event.getId(),
            user.getEmail(),
            new Date(),
            new Date(),
            new Date(),
            totalPrice,
            discount,
            totalPrice.subtract(discount),
            "CONFIRMED",
            tickets.stream().map(Ticket::getId).collect(Collectors.toList())
        );
    }

    private BigDecimal calculateDiscount(User user, BigDecimal totalPrice) {
        return bookingDAO.countBookingsByUser(user.getId()) > 5 ?
            totalPrice.multiply(new BigDecimal("0.05")) : BigDecimal.ZERO;
    }

    // Getters for metrics
    public int getSuccessfulBookings() { return successfulBookings.get(); }
    public int getFailedBookings() { return failedBookings.get(); }
    public int getConcurrencyConflicts() { return concurrencyConflicts.get(); }
    public int getDynamicFieldUpdates() { return dynamicFieldUpdates.get(); }
}