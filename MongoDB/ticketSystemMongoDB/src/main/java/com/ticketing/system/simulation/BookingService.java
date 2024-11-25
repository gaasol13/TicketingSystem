package com.ticketing.system.simulation;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.bson.types.ObjectId;
import com.mongodb.client.ClientSession;
import com.poortoys.examples.dao.*;
import com.ticketing.system.entities.*;
import dev.morphia.Datastore;

/**
 * Service class handling ticket booking operations using MongoDB.
 * Focuses on concurrent booking operations with transaction management.
 */
public class BookingService {
    private final BookingDAO bookingDAO;
    private final TicketDAO ticketDAO;
    private final UserDAO userDAO;
    private final EventDAO eventDAO;
    private final Datastore datastore;

    // Metrics
    private final AtomicInteger successfulBookings = new AtomicInteger(0);
    private final AtomicInteger failedBookings = new AtomicInteger(0);
    private final AtomicInteger totalTicketsBooked = new AtomicInteger(0);
    private long totalQueryTime = 0;
    private int totalQueries = 0;

    public BookingService(BookingDAO bookingDAO, TicketDAO ticketDAO,
                         UserDAO userDAO, EventDAO eventDAO, Datastore datastore) {
        this.bookingDAO = bookingDAO;
        this.ticketDAO = ticketDAO;
        this.userDAO = userDAO;
        this.eventDAO = eventDAO;
        this.datastore = datastore;
    }

    public boolean bookTickets(ObjectId userId, ObjectId eventId, int quantity) {
        System.out.println("User " + userId + " attempting to book " + 
                          quantity + " tickets for event " + eventId);

        try (ClientSession session = datastore.startSession()) {
            session.startTransaction();
            long queryStartTime = System.nanoTime();

            try {
                Event event = findAndValidateEvent(eventId);
                validateTicketAvailability(eventId, quantity);
                List<Ticket> bookedTickets = bookAvailableTickets(session, eventId, quantity);
                validateBookedTickets(bookedTickets, quantity);
                
                BigDecimal totalPrice = calculateTotalPrice(event, bookedTickets);
                String userEmail = getUserEmail(userId);
                
                Booking booking = createBooking(userId, eventId, userEmail, 
                                              totalPrice, bookedTickets);
                persistBooking(booking);

                session.commitTransaction();
                updateMetrics(bookedTickets.size());
                
                System.out.println("Booking successful for user " + userId + 
                    ". Total successful bookings: " + successfulBookings.get());
                return true;

            } catch (Exception e) {
                handleTransactionError(session, userId, e);
                return false;
            } finally {
                recordQueryTime(queryStartTime);
            }
        }
    }

    private Event findAndValidateEvent(ObjectId eventId) {
        long startTime = System.nanoTime();
        Event event = eventDAO.findById(eventId);
        recordQueryTime(startTime);

        if (event == null) {
            System.out.println("Event not found: " + eventId);
            throw new RuntimeException("Event not found: " + eventId);
        }
        return event;
    }

    private void validateTicketAvailability(ObjectId eventId, int quantity) {
        long startTime = System.nanoTime();
        long availableTickets = ticketDAO.countAvailableTickets(eventId);
        recordQueryTime(startTime);

        if (availableTickets < quantity) {
            throw new RuntimeException("Not enough tickets available for event: " + eventId);
        }
    }

    private List<Ticket> bookAvailableTickets(ClientSession session, 
                                            ObjectId eventId, int quantity) {
        long startTime = System.nanoTime();
        List<Ticket> bookedTickets = ticketDAO.bookAvailableTickets(session, 
                                                                   eventId, quantity);
        recordQueryTime(startTime);
        return bookedTickets;
    }

    private void validateBookedTickets(List<Ticket> bookedTickets, int quantity) {
        if (bookedTickets.size() < quantity) {
            throw new RuntimeException("Failed to book the desired number of tickets. " + 
                "Requested: " + quantity + ", Booked: " + bookedTickets.size());
        }
    }

    private BigDecimal calculateTotalPrice(Event event, List<Ticket> bookedTickets) {
        BigDecimal total = BigDecimal.ZERO;
        for (Ticket ticket : bookedTickets) {
            BigDecimal price = getPriceForCategory(event, ticket.getTicketCategory());
            total = total.add(price);
        }
        return total;
    }

    private BigDecimal getPriceForCategory(Event event, String ticketCategory) {
        return event.getTicketCategories().stream()
                .filter(tc -> tc.getDescription().equalsIgnoreCase(ticketCategory))
                .map(tc -> tc.getPrice())
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    private String getUserEmail(ObjectId userId) {
        long startTime = System.nanoTime();
        try {
            User user = userDAO.findById(userId);
            return user != null ? user.getEmail() : "unknown@example.com";
        } finally {
            recordQueryTime(startTime);
        }
    }

    private Booking createBooking(ObjectId userId, ObjectId eventId, String userEmail,
                                BigDecimal totalPrice, List<Ticket> bookedTickets) {
        return new Booking(
            userId,
            eventId,
            userEmail,
            new Date(), // deliveryTime
            new Date(), // timePaid
            new Date(), // timeSent
            totalPrice,
            BigDecimal.ZERO, // discount
            totalPrice, // finalPrice
            "confirmed",
            extractTicketIds(bookedTickets)
        );
    }

    private void persistBooking(Booking booking) {
        long startTime = System.nanoTime();
        try {
            bookingDAO.create(booking);
        } finally {
            recordQueryTime(startTime);
        }
    }

    private List<ObjectId> extractTicketIds(List<Ticket> tickets) {
        List<ObjectId> ticketIds = new ArrayList<>();
        for (Ticket ticket : tickets) {
            ticketIds.add(ticket.getId());
        }
        return ticketIds;
    }

    private void handleTransactionError(ClientSession session, ObjectId userId, Exception e) {
        System.err.println("Error during booking: " + e.getMessage());
        e.printStackTrace();
        session.abortTransaction();
        failedBookings.incrementAndGet();
        System.out.println("Booking failed for user " + userId + 
                         ". Total failed bookings: " + failedBookings.get());
    }

    private void updateMetrics(int ticketsBooked) {
        successfulBookings.incrementAndGet();
        totalTicketsBooked.addAndGet(ticketsBooked);
    }

    private void recordQueryTime(long startTime) {
        long endTime = System.nanoTime();
        totalQueryTime += (endTime - startTime);
        totalQueries++;
    }

    // Metrics retrieval methods
    public int getSuccessfulBookings() {
        return successfulBookings.get();
    }

    public int getFailedBookings() {
        return failedBookings.get();
    }

    public int getTotalTicketsBooked() {
        return totalTicketsBooked.get();
    }

    public double getAverageQueryTime() {
        return totalQueries > 0 ? (double) totalQueryTime / totalQueries / 1_000_000 : 0; // Convert to milliseconds
    }

    public int getTotalQueries() {
        return totalQueries;
    }

    /**
     * Validate current database state for consistency checking
     */
    public Map<String, Object> validateDatabaseState(ObjectId eventId) {
        Map<String, Object> state = new HashMap<>();
        
        try {
            long availableTickets = ticketDAO.countAvailableTickets(eventId);
            long totalBookings = bookingDAO.count();
            Event event = eventDAO.findById(eventId);
            
            state.put("availableTickets", availableTickets);
            state.put("totalBookings", totalBookings);
            state.put("eventName", event != null ? event.getName() : "Unknown");
            state.put("totalSuccessfulBookings", successfulBookings.get());
            state.put("totalFailedBookings", failedBookings.get());
            state.put("averageQueryTime", getAverageQueryTime());
            state.put("totalQueries", totalQueries);
            
        } catch (Exception e) {
            System.err.println("Error validating database state: " + e.getMessage());
            state.put("error", e.getMessage());
        }
        
        return state;
    }
    
    /**
     * Check if a specific booking exists and is valid
     */
    public boolean validateBooking(ObjectId bookingId) {
        try {
            Booking booking = bookingDAO.findById(bookingId);
            if (booking == null) {
                return false;
            }
            
            // Verify all tickets in the booking
            List<ObjectId> ticketIds = booking.getTickets();
            for (ObjectId ticketId : ticketIds) {
                Ticket ticket = ticketDAO.findById(ticketId);
                if (ticket == null || !"SOLD".equals(ticket.getStatus())) {
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error validating booking: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get detailed metrics about bookings for analysis
     */
    public BookingMetrics getDetailedMetrics() {
        return new BookingMetrics(
            successfulBookings.get(),
            failedBookings.get(),
            totalTicketsBooked.get(),
            getAverageQueryTime(),
            totalQueries
        );
    }
    
    /**
     * Inner class to hold detailed booking metrics
     */
    public static class BookingMetrics {
        private final int successfulBookings;
        private final int failedBookings;
        private final int totalTicketsBooked;
        private final double averageQueryTime;
        private final int totalQueries;
        
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