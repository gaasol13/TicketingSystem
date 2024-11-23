package com.poortoys.examples.simulation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.*;

import com.poortoys.examples.dao.BookingDAO;
import com.poortoys.examples.dao.BookingTicketDAO;
import com.poortoys.examples.dao.EventDAO;
import com.poortoys.examples.dao.TicketDAO;
import com.poortoys.examples.dao.UserDAO;
import com.poortoys.examples.entities.Booking;
import com.poortoys.examples.entities.BookingStatus;
import com.poortoys.examples.entities.BookingTicket;
import com.poortoys.examples.entities.Ticket;
import com.poortoys.examples.entities.TicketCategory;
import com.poortoys.examples.entities.TicketStatus;
import com.poortoys.examples.entities.User;

/**
 * Service class handling ticket booking operations using JPA/MySQL.
 * Demonstrates MySQL's capabilities in transaction management and schema modifications.
 */
public class BookingService {

    private final EntityManager em;
    private final UserDAO userDAO;
    private final TicketDAO ticketDAO;
    private final BookingDAO bookingDAO;
    private final BookingTicketDAO bookingTicketDAO;
    private final EventDAO eventDAO;

    // Metrics fields
    private final AtomicInteger successfulBookings = new AtomicInteger(0);
    private final AtomicInteger failedBookings = new AtomicInteger(0);
    private long totalQueryTime = 0;
    private int totalQueries = 0;

    /**
     * Constructor for BookingService.
     *
     * @param em               EntityManager for JPA operations
     * @param userDAO          DAO for user-related operations
     * @param ticketDAO        DAO for ticket-related operations
     * @param bookingDAO       DAO for booking-related operations
     * @param bookingTicketDAO DAO for booking-ticket associations
     * @param eventDAO         DAO for event-related operations
     */
    public BookingService(EntityManager em, UserDAO userDAO, TicketDAO ticketDAO,
                          BookingDAO bookingDAO, BookingTicketDAO bookingTicketDAO, EventDAO eventDAO) {
        this.em = em;
        this.userDAO = userDAO;
        this.ticketDAO = ticketDAO;
        this.bookingDAO = bookingDAO;
        this.bookingTicketDAO = bookingTicketDAO;
        this.eventDAO = eventDAO;

        try {
            // Verify database connection by executing a simple query
            em.createNativeQuery("SELECT 1").getSingleResult();
            System.out.println("Database connection verified in BookingService.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify database connection", e);
        }
    }

    /**
     * Retrieves available tickets using MySQL's query capabilities.
     *
     * @param eventId ID of the event for which to get available tickets
     * @return List of available ticket serial numbers
     */
    public List<String> getAvailableTicketSerials(int eventId) {
        long startTime = System.nanoTime();
        try {
            return em.createQuery(
                    "SELECT t.serialNumber FROM Ticket t " +
                            "WHERE t.event.eventId = :eventId AND t.status = :status", String.class)
                    .setParameter("eventId", eventId)
                    .setParameter("status", TicketStatus.AVAILABLE)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Error getting available tickets: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            long endTime = System.nanoTime();
            totalQueryTime += (endTime - startTime);
            totalQueries++;
        }
    }

    /**
     * POSITIVE SCENARIO: Demonstrates MySQL's ACID compliance
     * with pessimistic locking for concurrent bookings.
     */
    public Booking createBooking(int userId, List<String> ticketSerialNumbers, String deliveryEmail) throws Exception {
        EntityTransaction transaction = em.getTransaction();
        List<Ticket> lockedTickets = new ArrayList<>();
        long queryStartTime = System.nanoTime();

        try {
            transaction.begin();

            // Measure time for finding user
            long startTime = System.nanoTime();
            User user = em.find(User.class, userId);
            long endTime = System.nanoTime();
            totalQueryTime += (endTime - startTime);
            totalQueries++;

            if (user == null) {
                throw new RuntimeException("User not found: " + userId);
            }

            BigDecimal totalPrice = BigDecimal.ZERO;

            // Lock and validate each ticket
            for (String serial : ticketSerialNumbers) {
                try {
                    // Measure time for locking ticket
                    startTime = System.nanoTime();
                    TypedQuery<Ticket> query = em.createQuery(
                            "SELECT t FROM Ticket t " +
                                    "LEFT JOIN FETCH t.ticketCategory tc " +
                                    "WHERE t.serialNumber = :serial AND t.status = :status",
                            Ticket.class);
                    query.setParameter("serial", serial);
                    query.setParameter("status", TicketStatus.AVAILABLE);
                    query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
                    query.setHint("javax.persistence.lock.timeout", 5000);
                    Ticket ticket = query.getSingleResult();
                    endTime = System.nanoTime();
                    totalQueryTime += (endTime - startTime);
                    totalQueries++;

                    // Get the ticket category
                    TicketCategory category = ticket.getTicketCategory();
                    if (category == null) {
                        throw new RuntimeException("Ticket " + serial + " has no category assigned");
                    }

                    // Update the ticket status to 'RESERVED'
                    ticket.setStatus(TicketStatus.RESERVED);
                    // Add the ticket price to the total price
                    totalPrice = totalPrice.add(category.getPrice());
                    // Add the ticket to the list of locked tickets
                    lockedTickets.add(ticket);
                    // Merge the changes to the ticket entity
                    startTime = System.nanoTime();
                    em.merge(ticket);
                    endTime = System.nanoTime();
                    totalQueryTime += (endTime - startTime);
                    totalQueries++;

                } catch (NoResultException e) {
                    throw new Exception("Ticket not available: " + serial);
                } catch (PessimisticLockException e) {
                    throw new Exception("Concurrent access detected for ticket: " + serial);
                }
            }

            // Create a new Booking object
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setDeliveryAddressEmail(deliveryEmail);
            booking.setBookingTime(new Date());
            booking.setTotalPrice(totalPrice);
            booking.setDiscount(BigDecimal.ZERO);
            booking.setFinalPrice(totalPrice);
            booking.setBookingStatus(BookingStatus.CONFIRMED);

            // Measure time for persisting booking
            long startTimeBooking = System.nanoTime();
            em.persist(booking);
            em.flush();
            long endTimeBooking = System.nanoTime();
            totalQueryTime += (endTimeBooking - startTimeBooking);
            totalQueries++;

            // Create associations between the booking and each ticket
            for (Ticket ticket : lockedTickets) {
                BookingTicket bookingTicket = new BookingTicket();
                bookingTicket.setBooking(booking);
                bookingTicket.setTicket(ticket);
                // Measure time for persisting bookingTicket
                startTime = System.nanoTime();
                em.persist(bookingTicket);
                endTime = System.nanoTime();
                totalQueryTime += (endTime - startTime);
                totalQueries++;

                // Update the ticket status to 'SOLD' and set the purchase date
                ticket.setStatus(TicketStatus.SOLD);
                ticket.setPurchaseDate(new Date());
                // Measure time for merging ticket
                startTime = System.nanoTime();
                em.merge(ticket);
                endTime = System.nanoTime();
                totalQueryTime += (endTime - startTime);
                totalQueries++;
            }

            em.flush();
            transaction.commit();
            successfulBookings.incrementAndGet();
            return booking;

        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            failedBookings.incrementAndGet();
            throw e;
        } finally {
            long queryEndTime = System.nanoTime();
            totalQueryTime += (queryEndTime - queryStartTime);
            totalQueries++;
        }
    }

    /**
     * This method changes the database structure.
     * It can add or remove columns from tables.
     *
     * @param operation The operation to perform ('drop_user_columns' or 'add_user_columns')
     * @return Time taken to perform the operation in milliseconds
     */
    public long modifySchema(String operation) {
        EntityTransaction transaction = em.getTransaction();
        long startTime = System.nanoTime();

        try {
            transaction.begin();
            System.out.println("Starting schema modification: " + operation);

            String sql;
            switch (operation.toLowerCase()) {
                case "drop_user_columns":
                    sql = "ALTER TABLE users DROP COLUMN confirmation_code, DROP COLUMN confirmation_time";
                    break;
                case "add_user_columns":
                    sql = "ALTER TABLE users ADD COLUMN confirmation_code VARCHAR(100), ADD COLUMN confirmation_time DATETIME";
                    break;
                default:
                    throw new IllegalArgumentException("Unknown operation: " + operation);
            }

            System.out.println("Executing SQL: " + sql);
            long queryStartTime = System.nanoTime();
            em.createNativeQuery(sql).executeUpdate();
            long queryEndTime = System.nanoTime();
            totalQueryTime += (queryEndTime - queryStartTime);
            totalQueries++;

            transaction.commit();

            long endTime = System.nanoTime();
            long timeTaken = (endTime - startTime) / 1_000_000; // Convert to ms
            System.out.println("Schema modification completed in " + timeTaken + " ms");
            return timeTaken;

        } catch (Exception e) {
            System.err.println("Schema modification failed: " + e.getMessage());
            if (transaction.isActive()) {
                try {
                    System.out.println("Rolling back changes...");
                    transaction.rollback();
                } catch (Exception rollbackError) {
                    System.err.println("Rollback failed: " + rollbackError.getMessage());
                }
            }
            throw new RuntimeException("Could not modify schema: " + e.getMessage(), e);
        }
    }

    // Getter methods for metrics

    public int getSuccessfulBookings() {
        return successfulBookings.get();
    }

    public int getFailedBookings() {
        return failedBookings.get();
    }

    public double getAverageQueryTime() {
        return totalQueries > 0 ? (double) totalQueryTime / totalQueries / 1_000_000 : 0;
    }

    public int getTotalQueries() {
        return totalQueries;
    }
}
