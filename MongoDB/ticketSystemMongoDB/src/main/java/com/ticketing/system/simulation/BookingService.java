/**
 * BookingService class handles all operations related to ticket booking in a MongoDB-based ticketing system.
 * This class ensures data consistency, handles transactions, calculates pricing, and records metrics for performance analysis.
 */
package com.ticketing.system.simulation;

// Importing necessary libraries for database interaction and entity management
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.bson.types.ObjectId;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.poortoys.examples.dao.*;
import com.ticketing.system.entities.*;
import dev.morphia.Datastore;

public class BookingService {

    // DAO instances for database interaction with Booking, Ticket, User, and Event collections
    private final BookingDAO bookingDAO;
    private final TicketDAO ticketDAO;
    private final UserDAO userDAO;
    private final EventDAO eventDAO;
    private final Datastore datastore;

    // Atomic counters for tracking performance metrics
    private final AtomicInteger successfulBookings = new AtomicInteger(0);
    private final AtomicInteger failedBookings = new AtomicInteger(0);
    private final AtomicInteger totalTicketsBooked = new AtomicInteger(0);

    // Variables for measuring database query times
    private long totalQueryTime = 0; // Total time spent on queries in nanoseconds
    private int totalQueries = 0;   // Total number of queries executed

    // Constructor for initializing the service with required DAOs and Datastore
    public BookingService(BookingDAO bookingDAO, TicketDAO ticketDAO, UserDAO userDAO,
                          EventDAO eventDAO, Datastore datastore) {
        this.bookingDAO = bookingDAO;
        this.ticketDAO = ticketDAO;
        this.userDAO = userDAO;
        this.eventDAO = eventDAO;
        this.datastore = datastore;
    }

    /**
     * Handles the ticket booking process using transactions.
     * Ensures that all operations (e.g., ticket allocation, booking creation) are atomic.
     * @param userId   ID of the user booking tickets
     * @param eventId  ID of the event for which tickets are being booked
     * @param quantity Number of tickets requested
     * @return boolean indicating success or failure of the booking
     */
    public boolean bookTickets(ObjectId userId, ObjectId eventId, int quantity) {
        // Setting transaction options for read/write concerns and isolation level
        TransactionOptions txnOptions = TransactionOptions.builder()
                .readPreference(ReadPreference.primary()) // Use the primary node for consistency
                .readConcern(ReadConcern.SNAPSHOT)       // Snapshot isolation to ensure data consistency
                .writeConcern(WriteConcern.MAJORITY)     // Ensure data is written to majority of nodes
                .build();

        // Starting a client session to manage the transaction
        try (ClientSession session = datastore.startSession()) {
            session.startTransaction(txnOptions); // Initiate a transaction

            long queryStartTime = System.nanoTime(); // Start time for measuring transaction duration

            try {
                // Step 1: Validate that the event exists and is valid
                Event event = findAndValidateEvent(eventId);

                // Step 2: Validate that the user exists and is eligible to book tickets
                User user = findAndValidateUser(userId);

                // Step 3: Attempt to book tickets atomically
                List<Ticket> bookedTickets = ticketDAO.bookAvailableTickets(session, eventId, quantity);
                if (bookedTickets.isEmpty()) { // If no tickets were booked, abort the transaction
                    handleTransactionError(session, userId, new RuntimeException("No tickets available"));
                    return false;
                }

                // Step 4: Calculate the total price of the booked tickets
                BigDecimal totalPrice = calculateTotalPrice(event, bookedTickets);

                // Step 5: Create and save a booking record in the database
                Booking booking = createBooking(userId, eventId, user.getEmail(), totalPrice, bookedTickets);
                bookingDAO.create(booking);

                // Commit the transaction upon successful execution of all operations
                session.commitTransaction();

                // Update the metrics to reflect a successful booking
                updateMetrics(bookedTickets.size());

                System.out.println("Booking successful for user " + userId);
                return true;

            } catch (Exception e) { // Handle exceptions during transaction execution
                handleTransactionError(session, userId, e);
                return false;
            } finally {
                recordQueryTime(queryStartTime); // Record the time taken for the transaction
                if (session.hasActiveTransaction()) {
                    session.abortTransaction(); // Ensure no active transactions are left open
                }
            }
        }
    }

    // Retrieves and validates an event based on its ID
    private Event findAndValidateEvent(ObjectId eventId) {
        long startTime = System.nanoTime();
        try {
            Event event = eventDAO.findById(eventId); // Fetch event from the database
            if (event == null) { // Validate the existence of the event
                throw new RuntimeException("Event not found: " + eventId);
            }
            return event;
        } finally {
            recordQueryTime(startTime); // Record the time taken to query the event
        }
    }

    // Retrieves and validates a user based on their ID
    private User findAndValidateUser(ObjectId userId) {
        long startTime = System.nanoTime();
        try {
            User user = userDAO.findById(userId); // Fetch user from the database
            if (user == null) { // Validate the existence of the user
                throw new RuntimeException("User not found: " + userId);
            }
            return user;
        } finally {
            recordQueryTime(startTime); // Record the time taken to query the user
        }
    }

    // Calculates the total price of the tickets based on their categories
    private BigDecimal calculateTotalPrice(Event event, List<Ticket> tickets) {
        BigDecimal total = BigDecimal.ZERO; // Initialize total price to zero
        for (Ticket ticket : tickets) { // Loop through each ticket
            BigDecimal price = getPriceForCategory(event, ticket.getTicketCategory()); // Get category price
            total = total.add(price); // Accumulate the ticket prices
        }
        return total;
    }

    // Retrieves the price of a ticket category for an event
    private BigDecimal getPriceForCategory(Event event, String category) {
        return event.getTicketCategories().stream()
                .filter(tc -> tc.getDescription().equalsIgnoreCase(category)) // Find matching category
                .map(tc -> tc.getPrice()) // Extract the price
                .findFirst() // Return the first match
                .orElse(BigDecimal.ZERO); // Default to zero if no match found
    }

    // Creates a booking object with the given details
    private Booking createBooking(ObjectId userId, ObjectId eventId, String email,
                                  BigDecimal totalPrice, List<Ticket> tickets) {
        Date now = new Date(); // Current timestamp
        return new Booking(
                userId, eventId, email, now, now, now, // Timestamps for booking
                totalPrice, BigDecimal.ZERO, totalPrice, // Pricing details
                "confirmed", extractTicketIds(tickets) // Booking status and ticket IDs
        );
    }

    // Extracts the IDs from a list of Ticket objects
    private List<ObjectId> extractTicketIds(List<Ticket> tickets) {
        List<ObjectId> ticketIds = new ArrayList<>();
        for (Ticket ticket : tickets) { // Loop through each ticket
            ticketIds.add(ticket.getId()); // Add ticket ID to the list
        }
        return ticketIds; // Return the list of ticket IDs
    }

    // Handles errors during a transaction and ensures proper rollback
    private void handleTransactionError(ClientSession session, ObjectId userId, Exception e) {
        System.err.println("Error during booking for user " + userId + ": " + e.getMessage());
        if (session.hasActiveTransaction()) { // Check if transaction is still active
            session.abortTransaction(); // Abort the transaction to roll back changes
        }
        failedBookings.incrementAndGet(); // Update failed bookings counter
    }

    // Updates the performance metrics after a successful booking
    private void updateMetrics(int ticketsBooked) {
        successfulBookings.incrementAndGet(); // Increment successful bookings counter
        totalTicketsBooked.addAndGet(ticketsBooked); // Increment total tickets booked counter
    }

    // Records the time taken for a database query
    private void recordQueryTime(long startTime) {
        long endTime = System.nanoTime();
        totalQueryTime += (endTime - startTime); // Accumulate query duration
        totalQueries++; // Increment the number of queries executed
    }

    // Retrieves detailed booking metrics
    public BookingMetrics getDetailedMetrics() {
        return new BookingMetrics(
                successfulBookings.get(), failedBookings.get(), totalTicketsBooked.get(),
                getAverageQueryTime(), totalQueries
        );
    }

    // Calculates the average query time in milliseconds
    public double getAverageQueryTime() {
        return totalQueries > 0 ? (double) totalQueryTime / totalQueries / 1_000_000 : 0; // Convert nanoseconds to ms
    }

    /**
     * Inner class for encapsulating booking metrics.
     */
    public static class BookingMetrics {
        private final int successfulBookings; // Number of successful bookings
        private final int failedBookings; // Number of failed bookings
        private final int totalTicketsBooked; // Total tickets booked
        private final double averageQueryTime; // Average query time in milliseconds
        private final int totalQueries; // Total number of queries executed

        // Constructor for initializing metrics
       

        
        public BookingMetrics(int successfulBookings, int failedBookings, 
                            int totalTicketsBooked, double averageQueryTime, 
                            int totalQueries) {
            this.successfulBookings = successfulBookings;
            this.failedBookings = failedBookings;
            this.totalTicketsBooked = totalTicketsBooked;
            this.averageQueryTime = averageQueryTime;
            this.totalQueries = totalQueries;
        }
        
        // Getters
        public int getSuccessfulBookings() { return successfulBookings; }
        public int getFailedBookings() { return failedBookings; }
        public int getTotalTicketsBooked() { return totalTicketsBooked; }
        public double getAverageQueryTime() { return averageQueryTime; }
        public int getTotalQueries() { return totalQueries; }
        
        @Override
        public String toString() {
            return String.format(
                "BookingMetrics{successful=%d, failed=%d, ticketsBooked=%d, " +
                "avgQueryTime=%.2fms, totalQueries=%d}",
                successfulBookings, failedBookings, totalTicketsBooked, 
                averageQueryTime, totalQueries
            );
        }
    }
}