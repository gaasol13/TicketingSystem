/**
 * BookingService handles the core logic for creating bookings and managing tickets.
 * This service interacts with a MySQL database via JPA to ensure transactional integrity
 * and uses metrics to track performance and booking statistics.
 */

package com.poortoys.examples.simulation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.persistence.*;
import com.poortoys.examples.entities.*;

public class BookingService {
    // EntityManager for database operations
    private final EntityManager em;

    // Metrics for tracking performance and outcomes
    private final AtomicInteger successfulBookings = new AtomicInteger(0); // Successful booking count
    private final AtomicInteger failedBookings = new AtomicInteger(0); // Failed booking count
    private final AtomicInteger totalTicketsBooked = new AtomicInteger(0); // Total tickets booked
    private long totalQueryTime = 0; // Accumulated query time
    private int totalQueries = 0; // Total queries executed

    /**
     * Constructor to initialize BookingService with an EntityManager.
     * @param em EntityManager for JPA database operations
     */
    public BookingService(EntityManager em) {
        this.em = em;
        verifyDatabaseConnection(); // Verify database connection on initialization
    }

    /**
     * Verifies database connectivity by executing a simple query.
     */
    private void verifyDatabaseConnection() {
        try {
            em.createNativeQuery("SELECT 1").getSingleResult();
            System.out.println("Database connection verified in BookingService.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify database connection", e);
        }
    }

    /**
     * Retrieves the serial numbers of available tickets for a specific event.
     * @param eventId ID of the event
     * @return List of available ticket serial numbers
     */
    public List<String> getAvailableTicketSerials(int eventId) {
        long startTime = System.nanoTime();
        try {
            return em.createQuery(
                "SELECT t.serialNumber FROM Ticket t " +
                "WHERE t.event.eventId = :eventId AND t.status = :status", 
                String.class)
                .setParameter("eventId", eventId)
                .setParameter("status", TicketStatus.AVAILABLE)
                .getResultList();
        } catch (Exception e) {
            System.err.println("Error getting available tickets: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            recordQueryTime(startTime);
        }
    }

    /**
     * Creates a booking for a user, locking tickets to ensure consistency.
     * @param userId ID of the user making the booking
     * @param ticketSerials List of ticket serial numbers to book
     * @param email Email address for booking confirmation
     * @return Booking object if successful, or throws an exception on failure
     */
    public synchronized Booking createBooking(int userId, List<String> ticketSerials, String email) {
        EntityTransaction tx = em.getTransaction();
        long startTime = System.nanoTime();
        totalTicketsBooked.addAndGet(ticketSerials.size());

        try {
            tx.begin();

            // Step 1: Validate user existence
            User user = em.find(User.class, userId);
            if (user == null) {
                throw new RuntimeException("User not found: " + userId);
            }

            // Step 2: Lock tickets and calculate total price
            List<Ticket> tickets = new ArrayList<>();
            BigDecimal totalPrice = BigDecimal.ZERO;

            for (String serial : ticketSerials) {
                Ticket ticket = em.createQuery(
                    "SELECT t FROM Ticket t " +
                    "LEFT JOIN FETCH t.ticketCategory tc " +
                    "WHERE t.serialNumber = :serial AND t.status = :status",
                    Ticket.class)
                    .setParameter("serial", serial)
                    .setParameter("status", TicketStatus.AVAILABLE)
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE) // Lock ticket for updates
                    .setHint("javax.persistence.lock.timeout", 5000) // Lock timeout
                    .getSingleResult();

                if (ticket.getTicketCategory() == null) {
                    throw new RuntimeException("Ticket has no category: " + serial);
                }

                totalPrice = totalPrice.add(ticket.getTicketCategory().getPrice());
                ticket.setStatus(TicketStatus.SOLD);
                ticket.setPurchaseDate(new Date());
                tickets.add(ticket);
                em.merge(ticket); // Update ticket status in the database
            }

            // Step 3: Create and persist the booking
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setDeliveryAddressEmail(email);
            booking.setBookingTime(new Date());
            booking.setTotalPrice(totalPrice);
            booking.setDiscount(BigDecimal.ZERO);
            booking.setFinalPrice(totalPrice);
            booking.setBookingStatus(BookingStatus.CONFIRMED);

            em.persist(booking);

            // Step 4: Associate tickets with the booking
            for (Ticket ticket : tickets) {
                BookingTicket bookingTicket = new BookingTicket();
                bookingTicket.setBooking(booking);
                bookingTicket.setTicket(ticket);
                em.persist(bookingTicket);
            }

            em.flush(); // Persist all changes
            tx.commit(); // Commit the transaction
            
            successfulBookings.incrementAndGet();
            return booking;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback(); // Rollback transaction on failure
            }
            failedBookings.incrementAndGet();
            throw new RuntimeException("Booking failed: " + e.getMessage(), e);
        } finally {
            recordQueryTime(startTime);
        }
    }

    /**
     * Locks tickets for booking with pessimistic locking to ensure availability.
     * @param serials List of ticket serial numbers
     * @return List of locked Ticket objects
     */
    public List<Ticket> lockTickets(List<String> serials) {
        return em.createQuery(
            "SELECT t FROM Ticket t WHERE t.serialNumber IN :serials AND t.status = :status",
            Ticket.class)
            .setParameter("serials", serials)
            .setParameter("status", TicketStatus.AVAILABLE)
            .setLockMode(LockModeType.PESSIMISTIC_WRITE) // Apply pessimistic locking
            .getResultList();
    }

    /**
     * Records the time taken for queries to track performance metrics.
     * @param startTime Start time of the query in nanoseconds
     */
    private void recordQueryTime(long startTime) {
        long endTime = System.nanoTime();
        totalQueryTime += (endTime - startTime);
        totalQueries++;
    }

    // Getter methods for performance and booking metrics
    public int getSuccessfulBookings() {
        return successfulBookings.get();
    }

    public int getFailedBookings() {
        return failedBookings.get();
    }

    public double getAverageQueryTime() {
        return totalQueries > 0 ? (double) totalQueryTime / totalQueries / 1_000_000 : 0; // Convert to milliseconds
    }

    public int getTotalQueries() {
        return totalQueries;
    }
}
