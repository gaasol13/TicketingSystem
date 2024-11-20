package com.porrtoys.examples.simulation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PessimisticLockException;

import com.poortoys.examples.dao.*;
import com.poortoys.examples.entities.*;

/**
 * Service class handling ticket booking operations using JPA/MySQL.
 * Demonstrates MySQL's approach to:
 * 1. Transaction management in concurrent scenarios
 * 2. Relational data handling with foreign keys
 * 3. Pessimistic locking for handling race conditions
 */
public class BookingService {
    // DAOs demonstrate MySQL's structured approach to data access
    private final EntityManager em;
    private final UserDAO userDAO;
    private final TicketDAO ticketDAO;
    private final BookingDAO bookingDAO;
    private final BookingTicketDAO bookingTicketDAO;
    private final EventDAO eventDAO;
    
    // Thread-safe counters for monitoring concurrent operations
    private final AtomicInteger successfulBookings = new AtomicInteger(0);
    private final AtomicInteger failedBookings = new AtomicInteger(0);
    
    public BookingService(EntityManager em, UserDAO userDAO, TicketDAO ticketDAO, 
            BookingDAO bookingDAO, BookingTicketDAO bookingTicketDAO, EventDAO eventDAO) {
        this.em = em;
        this.userDAO = userDAO;
        this.ticketDAO = ticketDAO;
        this.bookingDAO = bookingDAO;
        this.bookingTicketDAO = bookingTicketDAO;
        this.eventDAO = eventDAO;
    }
    
    /**
     * Retrieves available tickets using MySQL's query capabilities
     * Demonstrates:
     * - JPA query language for relational data
     * - MySQL's ability to filter and join tables efficiently
     */
    public List<String> getAvailableTicketSerials(int eventId) {
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
        }
    }
    
    /**
     * POSITIVE SCENARIO: Demonstrates MySQL's ACID compliance
     * with pessimistic locking for concurrent bookings
     */
    public Booking createBooking(int userId, List<String> ticketSerialNumbers, 
                               String deliveryEmail) {
        EntityTransaction transaction = em.getTransaction();
        List<Ticket> lockedTickets = new ArrayList<>();
        
        try {
            // Step 1: Start transaction
            transaction.begin();
            
            // Step 2: Validate user
            User user = userDAO.findById(userId);
            if (user == null) {
                throw new RuntimeException("User not found: " + userId);
            }

            // Step 3: Lock and validate tickets
            for (String serial : ticketSerialNumbers) {
                try {
                    // Use pessimistic locking
                    Ticket ticket = em.createQuery(
                        "SELECT t FROM Ticket t " +
                        "WHERE t.serialNumber = :serial " +
                        "AND t.status = :status",
                        Ticket.class)
                        .setParameter("serial", serial)
                        .setParameter("status", TicketStatus.AVAILABLE)
                        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                        .getSingleResult();
                    
                    ticket.setStatus(TicketStatus.RESERVED);
                    em.merge(ticket);
                    lockedTickets.add(ticket);
                } catch (NoResultException e) {
                    throw new RuntimeException("Ticket not available: " + serial);
                }
            }

            // Step 4: Create booking
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setDeliveryAddressEmail(deliveryEmail);
            booking.setBookingTime(new Date());
            booking.setTotalPrice(calculateTotalPrice(lockedTickets));
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            
            em.persist(booking);

            // Step 5: Update ticket status
            for (Ticket ticket : lockedTickets) {
                ticket.setStatus(TicketStatus.SOLD);
                ticket.setPurchaseDate(new Date()); // Use the current date
                em.merge(ticket);
            }

            // Step 6: Commit transaction
            transaction.commit();
            successfulBookings.incrementAndGet();
            return booking;

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            failedBookings.incrementAndGet();
            throw new RuntimeException("Booking failed: " + e.getMessage(), e);
        }
    }

    /**
     * NEGATIVE SCENARIO: Demonstrates challenges with schema modifications
     * Returns time taken to modify schema in milliseconds
     */
    public long modifyTicketSchema(String newColumnName, String columnType) {
        EntityTransaction transaction = em.getTransaction();
        long startTime = System.currentTimeMillis();
        
        try {
            transaction.begin();
            
            // Execute schema modification
            em.createNativeQuery(
                "ALTER TABLE tickets " +
                "ADD COLUMN " + newColumnName + " " + columnType)
                .executeUpdate();
            
            transaction.commit();
            return System.currentTimeMillis() - startTime;
            
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Schema modification failed: " + 
                                     e.getMessage(), e);
        }
    }

    /**
     * Helper method to calculate total price of tickets
     */
    private BigDecimal calculateTotalPrice(List<Ticket> tickets) {
        return tickets.stream()
            .map(Ticket::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Getter methods for metrics
    public int getSuccessfulBookings() {
        return successfulBookings.get();
    }

    public int getFailedBookings() {
        return failedBookings.get();
    }
}
