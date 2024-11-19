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
     * Main booking method demonstrating MySQL's transaction handling
     * Shows how MySQL manages concurrent access through:
     * - Pessimistic locking
     * - ACID compliance
     * - Transaction isolation
     */
    public Booking createBooking(int userId, List<String> ticketSerialNumbers, String deliveryEmail) {
        EntityTransaction transaction = em.getTransaction();
        List<Ticket> lockedTickets = new ArrayList<>();
        
        try {
            // Verify user exists before starting transaction
            User user = userDAO.findById(userId);
            if(user == null) {
                failedBookings.incrementAndGet();
                throw new RuntimeException("User not found with ID: " + userId);
            }
            
            // Begin transaction for atomic operations
            transaction = em.getTransaction();
            transaction.begin();
            
            // 2. Get available tickets first - match MongoDB pattern
            List<Ticket> availableTickets = em.createQuery(
                "SELECT t FROM Ticket t WHERE t.serialNumber IN :serials AND t.status = :status", 
                Ticket.class)
                .setParameter("serials", ticketSerialNumbers)
                .setParameter("status", TicketStatus.AVAILABLE)
                .getResultList();

            if (availableTickets.size() < ticketSerialNumbers.size()) {
                throw new RuntimeException("Not enough tickets available");
            }
            
            // Demonstrate MySQL's pessimistic locking for concurrent access control
            for (String serialNumber : ticketSerialNumbers) {
                try {
                    Ticket ticket = em.createQuery(
                        "SELECT t FROM Ticket t " +
                        "WHERE t.serialNumber = :serialNumber " +
                        "AND t.status = :status", Ticket.class)
                        .setParameter("serialNumber", serialNumber)
                        .setParameter("status", TicketStatus.AVAILABLE)
                        .setLockMode(LockModeType.PESSIMISTIC_WRITE) // Key for concurrency control
                        .getSingleResult();
                    
                    lockedTickets.add(ticket);
                } catch (NoResultException nre) {
                    throw new RuntimeException("Ticket not available: " + serialNumber);
                }
            }
            
            // Validate locked tickets
            if (lockedTickets.isEmpty()) {
                throw new RuntimeException("No tickets could be locked for booking");
            }

            // Demonstrate relationship navigation in MySQL
            Event event = lockedTickets.get(0).getEvent();

            // Calculate pricing
            BigDecimal totalPrice = calculateTotalPrice(lockedTickets);
            BigDecimal discount = calculateDiscount(user, totalPrice);
            BigDecimal finalPrice = totalPrice.subtract(discount);

            // Create booking entity demonstrating MySQL's structured data model
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setDeliveryAddressEmail(deliveryEmail);
            booking.setDeliveryTime(new Date());
            booking.setTimePaid(new Date());
            booking.setTotalPrice(totalPrice);
            booking.setDiscount(discount);
            booking.setFinalPrice(finalPrice);
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            
            // Persist booking with transaction protection
            em.persist(booking);
            em.flush(); // Ensure booking is persisted before creating booking tickets

         // 5. Update tickets status within same transaction
            for (Ticket ticket : lockedTickets) {
                BookingTicket bookingTicket = new BookingTicket();
                bookingTicket.setBooking(booking);
                bookingTicket.setTicket(ticket);
                em.persist(bookingTicket);

                ticket.setStatus(TicketStatus.SOLD);
                ticket.setPurchaseDate(new Date());
                em.merge(ticket);
            }

            // Commit all changes atomically
            em.flush();
            transaction.commit();
            successfulBookings.incrementAndGet();
            
            return booking;

        } catch (PessimisticLockException e) {
            // Handle concurrent access conflicts
            System.err.println("Lock acquisition failed: " + e.getMessage());
            handleTransactionRollback(transaction);
            failedBookings.incrementAndGet();
            throw new RuntimeException("Could not lock all required tickets. Please try again.", e);
        } catch (Exception e) {
            System.err.println("Booking failed: " + e.getMessage());
            handleTransactionRollback(transaction);
            failedBookings.incrementAndGet();
            throw new RuntimeException("Booking failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Handles transaction rollback in case of failures
     * Demonstrates MySQL's transaction rollback capabilities
     */
    private void handleTransactionRollback(EntityTransaction transaction) {
        try {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
        } catch (Exception e) {
            System.err.println("Error during transaction rollback: " + e.getMessage());
        }
    }
    
    // Supporting methods...
    private BigDecimal calculateTotalPrice(List<Ticket> tickets) {
        return tickets.stream()
            .map(ticket -> ticket.getTicketCategory().getPrice())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calculateDiscount(User user, BigDecimal totalPrice) {
        List<Booking> userBookings = bookingDAO.findByUserId(user.getUserId());
        if (userBookings.size() > 5) {
            return totalPrice.multiply(new BigDecimal("0.05")); // 5% discount
        }
        return BigDecimal.ZERO;
    }

    // Metrics for monitoring concurrent operations
    public int getSuccessfulBookings() {
        return successfulBookings.get();
    }

    public int getFailedBookings() {
        return failedBookings.get();
    }
}