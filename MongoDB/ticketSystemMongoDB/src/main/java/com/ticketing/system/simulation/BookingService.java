package com.ticketing.system.simulation;

// Necessary imports for the service functionality
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
    
    // DAOs for interacting with different database collections
    private final BookingDAO bookingDAO;
    private final TicketDAO ticketDAO;
    private final UserDAO userDAO;
    private final EventDAO eventDAO;
    private final Datastore datastore;
    
    // Thread-safe counters for monitoring concurrent operations
    private AtomicInteger successfulBookings = new AtomicInteger(0);        // Counter for successful bookings
    private AtomicInteger failedBookings = new AtomicInteger(0);            // Counter for failed bookings
    private AtomicInteger concurrencyConflicts = new AtomicInteger(0);      // Counter for concurrency conflicts
    private AtomicInteger dynamicFieldUpdates = new AtomicInteger(0);       // Counter for dynamic field updates

    /**
     * Constructor for BookingService that initializes DAOs and the datastore.
     * 
     * @param bookingDAO DAO for bookings.
     * @param ticketDAO DAO for tickets.
     * @param userDAO DAO for users.
     * @param eventDAO DAO for events.
     * @param datastore Morphia Datastore object for interacting with MongoDB.
     */
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
     * by handling dynamic ticket categories and pricing.
     * 
     * @param eventId ID of the event for which the ticket is created.
     * @param dynamicFields Map of dynamic fields to be added to the ticket.
     * @return true if the ticket creation was successful, false otherwise.
     */
    public boolean createDynamicTicket(ObjectId eventId, Map<String, Object> dynamicFields) {
        try {
            long startTime = System.currentTimeMillis(); // Record the start time of the operation
            
            // Create a base ticket document with fixed fields
            Document ticketDoc = new Document()
                .append("eventId", eventId)           // ID of the associated event
                .append("status", "AVAILABLE")        // Initial status of the ticket
                .append("created", new Date());       // Creation date of the ticket
            
            // Dynamically add custom fields to the ticket document
            dynamicFields.forEach((key, value) -> {
                ticketDoc.append(key, value);        // Add each key-value pair to the document
                dynamicFieldUpdates.incrementAndGet(); // Increment the dynamic field updates counter
            });
            
            // Obtain the "tickets" collection directly from the datastore
            MongoCollection<Document> collection = datastore.getDatabase().getCollection("tickets");
            
            // Insert the ticket document into the collection
            collection.insertOne(ticketDoc);
            
            // Print success messages to the console
            System.out.println("Successfully created dynamic ticket with " + 
                dynamicFields.size() + " custom fields");
            System.out.println("Operation time: " + (System.currentTimeMillis() - startTime) + "ms");
            return true; // Indicate that the operation was successful
        } catch (Exception e) {
            // Handle exceptions in case of failure during ticket creation
            System.err.println("Failed to create dynamic ticket: " + e.getMessage());
            return false; // Indicate that the operation failed
        }
    }

    /**
     * NEGATIVE SCENARIO: Shows MongoDB's concurrency challenges during ticket booking.
     * - Client sessions for transactions.
     * - Document-level atomicity.
     * - Eventual consistency model.
     * 
     * @param userId ID of the user making the booking.
     * @param eventId ID of the event for which tickets are being booked.
     * @param quantity Number of tickets the user wants to book.
     * @return true if the booking was successful, false otherwise.
     */
    public boolean bookTickets(ObjectId userId, ObjectId eventId, int quantity) {
        long startTime = System.currentTimeMillis(); // Record the start time of the operation
        System.out.println("User " + userId + " attempting to book " + quantity + " tickets");
        
        // Start a client session to handle the transaction
        try (ClientSession session = datastore.startSession()) {
            session.startTransaction(); // Begin the transaction
            try {
                // 1. Validate user existence and check ticket availability
                User user = userDAO.findById(userId); // Find the user by ID
                if (user == null) {
                    recordFailure("User not found", startTime); // Record failure if user does not exist
                    return false;
                }

                Event event = eventDAO.findById(eventId); // Find the event by ID
                if (event == null) {
                    recordFailure("Event not found", startTime); // Record failure if event does not exist
                    return false;
                }

                // 2. Check ticket availability (potential race condition point)
                List<Ticket> availableTickets = ticketDAO.findAvailableTicketsByEventId(eventId);
                if (availableTickets.size() < quantity) {
                    recordFailure("Insufficient tickets", startTime); // Record failure if not enough tickets
                    return false;
                }

                // 3. Attempt to lock tickets (demonstration of concurrency challenges)
                List<Ticket> lockedTickets = new ArrayList<>();
                for (int i = 0; i < quantity; i++) {
                    try {
                        Ticket ticket = availableTickets.get(i); // Get an available ticket
                        // Attempt to atomically change the ticket status to "LOCKED"
                        Ticket lockedTicket = ticketDAO.findAndModifyTicket(session, ticket.getId(), "LOCKED");
                        if (lockedTicket == null) {
                            concurrencyConflicts.incrementAndGet(); // Increment the concurrency conflicts counter
                            throw new RuntimeException("Concurrent access detected for ticket: " + ticket.getId());
                        }
                        lockedTickets.add(lockedTicket); // Add the locked ticket to the list
                    } catch (Exception e) {
                        session.abortTransaction(); // Abort the transaction in case of error
                        recordFailure("Concurrent access conflict", startTime); // Record failure due to conflict
                        return false;
                    }
                }

                // 4. Create booking if ticket locking was successful
                BigDecimal totalPrice = calculateTotalPrice(event, lockedTickets); // Calculate total price
                Booking booking = createBooking(user, event, lockedTickets, totalPrice); // Create the booking entity
                bookingDAO.create(session, booking); // Persist the booking in the database

                // 5. Update ticket statuses to "SOLD" (another consistency challenge)
                for (Ticket ticket : lockedTickets) {
                    ticketDAO.update(session, ticket.getId(), "SOLD"); // Update the ticket status
                }

                session.commitTransaction(); // Commit the transaction
                recordSuccess("Booking completed successfully", startTime); // Record successful booking
                return true; // Indicate that the booking was successful

            } catch (Exception e) {
                session.abortTransaction(); // Abort the transaction in case of any exception
                recordFailure("Transaction failed: " + e.getMessage(), startTime); // Record the failure reason
                return false; // Indicate that the booking failed
            }
        }
    }

    /**
     * Helper method to record a successful booking.
     * 
     * @param message Descriptive message of the success.
     * @param startTime Start time of the operation to calculate duration.
     */
    private void recordSuccess(String message, long startTime) {
        successfulBookings.incrementAndGet(); // Increment the successful bookings counter
        long duration = System.currentTimeMillis() - startTime; // Calculate the operation duration
        System.out.println("✓ " + message + " (Duration: " + duration + "ms)"); // Print success message
    }

    /**
     * Helper method to record a failed booking.
     * 
     * @param reason Reason for the failure.
     * @param startTime Start time of the operation to calculate duration.
     */
    private void recordFailure(String reason, long startTime) {
        failedBookings.incrementAndGet(); // Increment the failed bookings counter
        long duration = System.currentTimeMillis() - startTime; // Calculate the operation duration
        System.out.println("✗ Failed: " + reason + " (Duration: " + duration + "ms)"); // Print failure message
    }

    /**
     * Helper method to calculate the total price of a booking.
     * 
     * @param event Event for which tickets are being booked.
     * @param tickets List of tickets being booked.
     * @return Total price of the booking.
     */
    private BigDecimal calculateTotalPrice(Event event, List<Ticket> tickets) {
        return tickets.stream()
            .map(ticket -> getPriceForCategory(event, ticket.getTicketCategory())) // Get price per category
            .reduce(BigDecimal.ZERO, BigDecimal::add); // Sum all prices
    }

    /**
     * Helper method to get the price for a specific ticket category.
     * 
     * @param event Event that contains ticket categories.
     * @param category Category of the ticket.
     * @return Price of the specified category.
     */
    private BigDecimal getPriceForCategory(Event event, String category) {
        return event.getTicketCategories().stream()
            .filter(tc -> tc.getDescription().equalsIgnoreCase(category)) // Filter by category description
            .map(tc -> tc.getPrice()) // Map to prices
            .findFirst() // Get the first matching category
            .orElse(BigDecimal.ZERO); // Return 0 if category not found
    }

    /**
     * Helper method to create a booking entity.
     * 
     * @param user User making the booking.
     * @param event Event for which tickets are being booked.
     * @param tickets List of tickets being booked.
     * @param totalPrice Total price of the booking before discounts.
     * @return Booking object representing the reservation.
     */
    private Booking createBooking(User user, Event event, List<Ticket> tickets, BigDecimal totalPrice) {
        BigDecimal discount = calculateDiscount(user, totalPrice); // Calculate applicable discount
        return new Booking(
            user.getId(), // User ID
            event.getId(), // Event ID
            user.getEmail(), // User email
            new Date(), // Booking creation date
            new Date(), // Booking modification date
            new Date(), // Booking confirmation date
            totalPrice, // Total price before discounts
            discount, // Applied discount
            totalPrice.subtract(discount), // Final price after discounts
            "CONFIRMED", // Booking status
            tickets.stream().map(Ticket::getId).collect(Collectors.toList()) // List of booked ticket IDs
        );
    }

    /**
     * Helper method to calculate the discount applicable to a booking.
     * 
     * @param user User making the booking.
     * @param totalPrice Total price of the booking before discounts.
     * @return Discount amount.
     */
    private BigDecimal calculateDiscount(User user, BigDecimal totalPrice) {
        // Apply a 5% discount if the user has made more than 5 bookings
        return bookingDAO.countBookingsByUser(user.getId()) > 5 ?
            totalPrice.multiply(new BigDecimal("0.05")) : BigDecimal.ZERO;
    }

    // Getter methods for metrics
    /**
     * Retrieves the total number of successful bookings.
     * 
     * @return Number of successful bookings.
     */
    public int getSuccessfulBookings() { return successfulBookings.get(); }

    /**
     * Retrieves the total number of failed bookings.
     * 
     * @return Number of failed bookings.
     */
    public int getFailedBookings() { return failedBookings.get(); }

    /**
     * Retrieves the total number of detected concurrency conflicts.
     * 
     * @return Number of concurrency conflicts.
     */
    public int getConcurrencyConflicts() { return concurrencyConflicts.get(); }

    /**
     * Retrieves the total number of dynamic field updates performed.
     * 
     * @return Number of dynamic field updates.
     */
    public int getDynamicFieldUpdates() { return dynamicFieldUpdates.get(); }
}
