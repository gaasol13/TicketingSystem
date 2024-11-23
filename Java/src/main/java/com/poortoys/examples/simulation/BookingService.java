package com.poortoys.examples.simulation; // hope I spelled that right

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PessimisticLockException;

import com.poortoys.examples.dao.*; // importing all DAOs
import com.poortoys.examples.entities.*; // importing all entities

/**
 * service class handling ticket booking operations using JPA/MySQL
 * demonstrates MySQL's approach to:
 * 1. transaction management in concurrent scenarios
 * 2. relational data handling with foreign keys
 * 3. pessimistic locking for handling race conditions
 */
public class BookingService {
    // DAOs for data access, I think this is the best way
    private final EntityManager em; // entity manager for managing entities and transactions
    private final UserDAO userDAO; // dao for user-related database operations
    private final TicketDAO ticketDAO; // dao for ticket-related database operations
    private final BookingDAO bookingDAO; // dao for booking-related database operations
    private final BookingTicketDAO bookingTicketDAO; // dao for booking-ticket relationship operations
    private final EventDAO eventDAO; // dao for event-related database operations
    
    // counters for monitoring concurrent operations
    private final AtomicInteger successfulBookings = new AtomicInteger(0); // successful bookings counter
    private final AtomicInteger failedBookings = new AtomicInteger(0); // failed bookings counter
    
    /**
     * constructor for BookingService
     * initializes the EntityManager and DAOs required for booking operations
     */
    
   //private final ThreadLocal<EntityManager> threadLocalEm = new ThreadLocal<>();
    
    public BookingService(EntityManager em, UserDAO userDAO, TicketDAO ticketDAO, 
                          BookingDAO bookingDAO, BookingTicketDAO bookingTicketDAO, EventDAO eventDAO) {
        this.em = em;
        this.userDAO = userDAO;
        this.ticketDAO = ticketDAO;
        this.bookingDAO = bookingDAO;
        this.bookingTicketDAO = bookingTicketDAO;
        this.eventDAO = eventDAO;
        
 
        
        try {
            em.createNativeQuery("SELECT 1").getSingleResult();
            System.out.println("Database connection verified in BookingService.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify database connection", e);
        }
    }
    
	/*
	 * private EntityManager getEntityManager() { EntityManager entityManager =
	 * threadLocalEm.get(); if (entityManager == null || !entityManager.isOpen()) {
	 * entityManager = emf.createEntityManager(); threadLocalEm.set(entityManager);
	 * } return entityManager; }
	 */
    /**
     * retrieves available tickets using MySQL's query capabilities
     * demonstrates:
     * - JPA query language for relational data
     * - MySQL's ability to filter and join tables efficiently
     */
    public List<String> getAvailableTicketSerials(int eventId) {
        try {
            // create and execute a JPQL query to select serial numbers of available tickets for the given event
            return em.createQuery(
                "SELECT t.serialNumber FROM Ticket t " +
                "WHERE t.event.eventId = :eventId AND t.status = :status", String.class)
                .setParameter("eventId", eventId) // set the eventId parameter
                .setParameter("status", TicketStatus.AVAILABLE) // set the status parameter to 'AVAILABLE'
                .getResultList(); // retrieve the result list
        } catch (Exception e) {
            // something went wrong getting available tickets
            System.err.println("Error getting available tickets: " + e.getMessage());
            return new ArrayList<>(); // return an empty list
        }
    }
    
    /**
     * POSITIVE SCENARIO: demonstrates MySQL's ACID compliance
     * with pessimistic locking for concurrent bookings
     */
    public synchronized Booking createBooking(int userId, List<String> ticketSerialNumbers, String deliveryEmail) {
        EntityTransaction transaction = em.getTransaction();
        List<Ticket> lockedTickets = new ArrayList<>();
        
        try {
        	 transaction.begin();
             
             // Get user with fresh EntityManager
             User user = em.find(User.class, userId);
             if (user == null) {
                 throw new RuntimeException("User not found: " + userId);
             }

            BigDecimal totalPrice = BigDecimal.ZERO;
            
            // Lock and validate tickets
            for (String serial : ticketSerialNumbers) {
                try {
                    System.out.println("Attempting to lock ticket: " + serial);
                    
                    // Updated query to match your table structure
                    Ticket ticket = em.createQuery(
                        "SELECT t FROM Ticket t " +
                        "LEFT JOIN FETCH t.ticketCategory tc " +
                        "WHERE t.serialNumber = :serial AND t.status = 'available'",
                        Ticket.class)
                        .setParameter("serial", serial)
                        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                        .setHint("javax.persistence.lock.timeout", 5000)
                        .getSingleResult();
                    
                    TicketCategory category = ticket.getTicketCategory();
                    if (category == null) {
                        throw new RuntimeException("Ticket " + serial + " has no category assigned");
                    }
                    
                    System.out.println("Ticket locked successfully: " + serial + 
                                     ", Area: " + category.getArea() + 
                                     ", Price: " + category.getPrice());
                    
                    ticket.setStatus(TicketStatus.RESERVED);  // Match your enum/string status
                    totalPrice = totalPrice.add(category.getPrice());
                    lockedTickets.add(ticket);
                    em.merge(ticket);
                    
                } catch (NoResultException e) {
                    System.err.println("Failed to find available ticket: " + serial);
                    throw new RuntimeException("Ticket not available: " + serial);
                } catch (PessimisticLockException e) {
                    System.err.println("Failed to lock ticket: " + serial);
                    throw new RuntimeException("Concurrent access detected for ticket: " + serial);
                }
            }

            System.out.println("All tickets locked. Creating booking with total price: " + totalPrice);

            // Create booking first
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setDeliveryAddressEmail(deliveryEmail);
            booking.setBookingTime(new Date());
            booking.setTotalPrice(totalPrice);
            booking.setDiscount(BigDecimal.ZERO);
            booking.setFinalPrice(totalPrice);
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            
            em.persist(booking);
            em.flush(); // Ensure booking is persisted first

            // Then create BookingTicket associations
            for (Ticket ticket : lockedTickets) {
                BookingTicket bookingTicket = new BookingTicket();
                bookingTicket.setBooking(booking);
                bookingTicket.setTicket(ticket);
                em.persist(bookingTicket); // Persist each BookingTicket
                
                ticket.setStatus(TicketStatus.SOLD);
                ticket.setPurchaseDate(new Date());
                em.merge(ticket);
            }

            em.flush();
            transaction.commit();
            System.out.println("Transaction committed successfully");
            
            successfulBookings.incrementAndGet();
            return booking;

        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Booking failed: " + e.getMessage(), e);
			/*
			 * } finally { // Clean up thread-local EntityManager EntityManager currentEm =
			 * threadLocalEm.get(); if (currentEm != null && currentEm.isOpen()) {
			 * currentEm.close(); } threadLocalEm.remove();
			 */
        }
    }
    
    /**
     * this method changes the database structure
     * can add or remove columns from tables
     */
    public long modifySchema(String operation) {
        EntityTransaction transaction = em.getTransaction();
        long startTime = System.currentTimeMillis();

        try {
            transaction.begin();
            System.out.println("Starting schema modification: " + operation);

            String sql;
            switch (operation.toLowerCase()) {
                case "drop_user_columns":
                    // drop confirmation columns
                    sql = "ALTER TABLE users DROP COLUMN confirmation_code, DROP COLUMN confirmation_time";
                    break;
                case "add_user_columns":
                    // add confirmation columns back
                    sql = "ALTER TABLE users ADD COLUMN confirmation_code VARCHAR(100), ADD COLUMN confirmation_time DATETIME";
                    break;
                default:
                    // unknown operation, can't proceed
                    throw new IllegalArgumentException("Unknown operation: " + operation);
            }

            System.out.println("Executing SQL: " + sql);
            em.createNativeQuery(sql).executeUpdate();

            transaction.commit();
            
            long timeTaken = System.currentTimeMillis() - startTime;
            System.out.println("Schema modification completed in " + timeTaken + "ms");
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
            
            // could not modify schema, rethrow exception
            throw new RuntimeException("Could not modify schema: " + e.getMessage(), e);
        }
    }

    //  can also create specific methods for each operation if you prefer
    public long dropUserColumns() {
        return modifySchema("drop_user_columns");
    }

    public long addUserColumns() {
        return modifySchema("add_user_columns");
    }

    /**
     *  method to calculate the total price of booked tickets
     */
    private BigDecimal calculateTotalPrice(List<Ticket> tickets) {
        // sum the prices of all tickets
        return tickets.stream()
            .map(Ticket::getPrice) // get price of each ticket
            .reduce(BigDecimal.ZERO, BigDecimal::add); // sum them up
    }
    
    // getter methods for metrics
    
    /**
     * retrieves the total number of successful bookings
     */
    public int getSuccessfulBookings() {
        return successfulBookings.get(); // get the value
    }

    /**
     * retrieves the total number of failed bookings
     * 
     * @return number of failed bookings
     */
    public int getFailedBookings() {
        return failedBookings.get(); // get the value
    }
}
