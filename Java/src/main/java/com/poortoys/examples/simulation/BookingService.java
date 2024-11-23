package com.poortoys.examples.simulation; // This declares the package name, kind of like the folder where this class lives

// Import statements to bring in necessary classes and interfaces
import java.math.BigDecimal; // For precise decimal calculations, especially with money
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger; // For thread-safe integer operations

import javax.persistence.EntityManager; // Manages the persistence context (entities)
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction; // For handling transactions
import javax.persistence.LockModeType; // For specifying locking strategies
import javax.persistence.NoResultException; // Exception when a query returns no results
import javax.persistence.PessimisticLockException; // Exception when a pessimistic lock fails

import com.poortoys.examples.dao.*; // Importing all Data Access Objects (DAOs)
import com.poortoys.examples.entities.*; // Importing all entity classes

/**
 * Service class handling ticket booking operations using JPA/MySQL.
 * Demonstrates MySQL's approach to:
 * 1. Transaction management in concurrent scenarios
 * 2. Relational data handling with foreign keys
 * 3. Pessimistic locking for handling race conditions
 */
public class BookingService {
    // DAOs for data access, I think this is the best way
    private final EntityManager em; // Manages entities and handles transactions
    private final UserDAO userDAO; // DAO for user-related database operations
    private final TicketDAO ticketDAO; // DAO for ticket-related database operations
    private final BookingDAO bookingDAO; // DAO for booking-related database operations
    private final BookingTicketDAO bookingTicketDAO; // DAO for booking-ticket relationship operations
    private final EventDAO eventDAO; // DAO for event-related database operations
    
    // Counters for monitoring concurrent operations
    private final AtomicInteger successfulBookings = new AtomicInteger(0); // Counts successful bookings
    private final AtomicInteger failedBookings = new AtomicInteger(0); // Counts failed bookings
    // Add these fields
    private long totalQueryTime = 0;
    private int totalQueries = 0;

    /**
     * Constructor for BookingService.
     * Initializes the EntityManager and DAOs required for booking operations.
     */
    public BookingService(EntityManager em, UserDAO userDAO, TicketDAO ticketDAO, 
                          BookingDAO bookingDAO, BookingTicketDAO bookingTicketDAO, EventDAO eventDAO) {
        this.em = em; // Assign the EntityManager
        this.userDAO = userDAO; // Assign the UserDAO
        this.ticketDAO = ticketDAO; // Assign the TicketDAO
        this.bookingDAO = bookingDAO; // Assign the BookingDAO
        this.bookingTicketDAO = bookingTicketDAO; // Assign the BookingTicketDAO
        this.eventDAO = eventDAO; // Assign the EventDAO
        
        try {
            // Verify database connection by executing a simple query
            em.createNativeQuery("SELECT 1").getSingleResult();
            System.out.println("Database connection verified in BookingService.");
        } catch (Exception e) {
            // If the query fails, throw a RuntimeException
            throw new RuntimeException("Failed to verify database connection", e);
        }
    }
    
    /*
     * This is a commented-out method that was perhaps used for thread-local EntityManagers.
     * Since it's commented out, it doesn't affect the current code.
     */
    /*
    private final ThreadLocal<EntityManager> threadLocalEm = new ThreadLocal<>();
    
    private EntityManager getEntityManager() {
        EntityManager entityManager = threadLocalEm.get();
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = emf.createEntityManager();
            threadLocalEm.set(entityManager);
        }
        return entityManager;
    }
    */
    
    /**
     * Retrieves available tickets using MySQL's query capabilities.
     * Demonstrates:
     * - JPA query language for relational data
     * - MySQL's ability to filter and join tables efficiently
     * 
     * @param eventId ID of the event for which to get available tickets
     * @return List of available ticket serial numbers
     */
    public List<String> getAvailableTicketSerials(int eventId) {
        try {
            // Create and execute a JPQL query to select serial numbers of available tickets for the given event
            return em.createQuery(
                "SELECT t.serialNumber FROM Ticket t " +
                "WHERE t.event.eventId = :eventId AND t.status = :status", String.class)
                .setParameter("eventId", eventId) // Set the eventId parameter
                .setParameter("status", TicketStatus.AVAILABLE) // Set the status parameter to 'AVAILABLE'
                .getResultList(); // Retrieve the result list
        } catch (Exception e) {
            // Something went wrong getting available tickets
            System.err.println("Error getting available tickets: " + e.getMessage());
            return new ArrayList<>(); // Return an empty list to indicate failure
        }
    }
    
    /**
     * POSITIVE SCENARIO: Demonstrates MySQL's ACID compliance
     * with pessimistic locking for concurrent bookings.
     */
    public synchronized Booking createBooking(int userId, List<String> ticketSerialNumbers, String deliveryEmail) {
        EntityTransaction transaction = em.getTransaction(); // Get the transaction object
        List<Ticket> lockedTickets = new ArrayList<>(); // List to keep track of locked tickets
        long queryStartTime = System.nanoTime();
        
        try {
            transaction.begin(); // Start the transaction
            
            // Get the user from the database using their ID
            User user = em.find(User.class, userId);
            if (user == null) {
                // If user not found, throw an exception
                throw new RuntimeException("User not found: " + userId);
            }

            BigDecimal totalPrice = BigDecimal.ZERO; // Initialize total price to zero
            
            // Lock and validate each ticket
            for (String serial : ticketSerialNumbers) {
                try {
                    System.out.println("Attempting to lock ticket: " + serial);
                    
                    // Query to select the ticket with the given serial number and status 'available'
                    Ticket ticket = em.createQuery(
                        "SELECT t FROM Ticket t " +
                        "LEFT JOIN FETCH t.ticketCategory tc " + // Fetch the associated ticket category
                        "WHERE t.serialNumber = :serial AND t.status = 'available'",
                        Ticket.class)
                        .setParameter("serial", serial) // Set the serial number parameter
                        .setLockMode(LockModeType.PESSIMISTIC_WRITE) // Apply a pessimistic write lock
                        .setHint("javax.persistence.lock.timeout", 5000) // Set lock timeout to 5 seconds
                        .getSingleResult(); // Execute the query and get the single result
                    
                    // Get the ticket category (e.g., VIP, General Admission)
                    TicketCategory category = ticket.getTicketCategory();
                    if (category == null) {
                        // If no category is assigned, throw an exception
                        throw new RuntimeException("Ticket " + serial + " has no category assigned");
                    }
                    
                    System.out.println("Ticket locked successfully: " + serial + 
                                     ", Area: " + category.getArea() + 
                                     ", Price: " + category.getPrice());
                    
                    // Update the ticket status to 'RESERVED'
                    ticket.setStatus(TicketStatus.RESERVED);  // Match your enum/string status
                    // Add the ticket price to the total price
                    totalPrice = totalPrice.add(category.getPrice());
                    // Add the ticket to the list of locked tickets
                    lockedTickets.add(ticket);
                    // Merge the changes to the ticket entity
                    em.merge(ticket);
                    
                } catch (NoResultException e) {
                    // If no ticket is found, print error and throw an exception
                    System.err.println("Failed to find available ticket: " + serial);
                    throw new RuntimeException("Ticket not available: " + serial);
                } catch (PessimisticLockException e) {
                    // If locking fails, print error and throw an exception
                    System.err.println("Failed to lock ticket: " + serial);
                    throw new RuntimeException("Concurrent access detected for ticket: " + serial);
                }
            }

            System.out.println("All tickets locked. Creating booking with total price: " + totalPrice);

            // Create a new Booking object
            Booking booking = new Booking();
            booking.setUser(user); // Set the user who made the booking
            booking.setDeliveryAddressEmail(deliveryEmail); // Set the delivery email
            booking.setBookingTime(new Date()); // Set the booking time to now
            booking.setTotalPrice(totalPrice); // Set the total price
            booking.setDiscount(BigDecimal.ZERO); // Assume no discount
            booking.setFinalPrice(totalPrice); // Final price after discount
            booking.setBookingStatus(BookingStatus.CONFIRMED); // Set booking status to 'CONFIRMED'
            
            em.persist(booking); // Persist the booking entity
            em.flush(); // Force synchronization with the database to get the booking ID

            // Create associations between the booking and each ticket
            for (Ticket ticket : lockedTickets) {
                BookingTicket bookingTicket = new BookingTicket();
                bookingTicket.setBooking(booking); // Set the booking
                bookingTicket.setTicket(ticket); // Set the ticket
                em.persist(bookingTicket); // Persist the BookingTicket entity
                
                // Update the ticket status to 'SOLD' and set the purchase date
                ticket.setStatus(TicketStatus.SOLD);
                ticket.setPurchaseDate(new Date());
                em.merge(ticket); // Merge changes to the ticket entity
            }
            
            

            em.flush(); // Ensure all changes are flushed to the database
            transaction.commit(); // Commit the transaction
            System.out.println("Transaction committed successfully");
            
            successfulBookings.incrementAndGet(); // Increment the successful bookings counter
            return booking; // Return the created booking

        } catch (Exception e) {
            // If any exception occurs, roll back the transaction
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            // Rethrow the exception with a descriptive message
            throw new RuntimeException("Booking failed: " + e.getMessage(), e);
        }
        
    }
    
    /**
     * This method changes the database structure.
     * It can add or remove columns from tables.
     * 
     * @param operation The operation to perform ('drop_user_columns' or 'add_user_columns')
     * @return          Time taken to perform the operation in milliseconds
     */
    public long modifySchema(String operation) {
        EntityTransaction transaction = em.getTransaction(); // Get the transaction object
        long startTime = System.currentTimeMillis(); // Record the start time

        try {
            transaction.begin(); // Start the transaction
            System.out.println("Starting schema modification: " + operation);

            String sql; // Variable to hold the SQL command
            switch (operation.toLowerCase()) {
                case "drop_user_columns":
                    // SQL to drop confirmation columns from the 'users' table
                    sql = "ALTER TABLE users ADD COLUMN confirmation_code, DROP COLUMN confirmation_time";
                    break;
                case "add_user_columns":
                    // SQL to add confirmation columns back to the 'users' table
                    sql = "ALTER TABLE users ADD COLUMN confirmation_code VARCHAR(100), ADD COLUMN confirmation_time DATETIME";
                    break;
                default:
                    // If the operation is unknown, throw an exception
                    throw new IllegalArgumentException("Unknown operation: " + operation);
            }

            System.out.println("Executing SQL: " + sql);
            em.createNativeQuery(sql).executeUpdate(); // Execute the SQL command

            transaction.commit(); // Commit the transaction
            
            long timeTaken = System.currentTimeMillis() - startTime; // Calculate time taken
            System.out.println("Schema modification completed in " + timeTaken + "ms");
            return timeTaken; // Return the time taken

        } catch (Exception e) {
            // If an exception occurs, print an error message
            System.err.println("Schema modification failed: " + e.getMessage());
            
            // Attempt to roll back the transaction if it's active
            if (transaction.isActive()) {
                try {
                    System.out.println("Rolling back changes...");
                    transaction.rollback();
                } catch (Exception rollbackError) {
                    // If rollback fails, print an error message
                    System.err.println("Rollback failed: " + rollbackError.getMessage());
                }
            }
            
            // Rethrow the exception
            throw new RuntimeException("Could not modify schema: " + e.getMessage(), e);
        }
    }

    // Convenience methods to perform specific schema modifications
    /**
     * Drops specific columns from the 'users' table.
     * 
     * @return Time taken to perform the operation in milliseconds
     */
    public long dropUserColumns() {
        return modifySchema("drop_user_columns");
    }

    /**
     * Adds specific columns back to the 'users' table.
     * 
     * @return Time taken to perform the operation in milliseconds
     */
    public long addUserColumns() {
        return modifySchema("add_user_columns");
    }

    /**
     * Method to calculate the total price of booked tickets.
     * 
     * @param tickets List of tickets
     * @return        Total price as BigDecimal
     */
    private BigDecimal calculateTotalPrice(List<Ticket> tickets) {
        // Sum the prices of all tickets
        return tickets.stream()
            .map(Ticket::getPrice) // Get the price of each ticket
            .reduce(BigDecimal.ZERO, BigDecimal::add); // Sum them up starting from zero
    }
    
    // Getter methods for metrics
    
    /**
     * Retrieves the total number of successful bookings.
     * 
     * @return Number of successful bookings
     */
    public int getSuccessfulBookings() {
        return successfulBookings.get(); // Get the current value atomically
    }

    /**
     * Retrieves the total number of failed bookings.
     * 
     * @return Number of failed bookings
     */
    public int getFailedBookings() {
        return failedBookings.get(); // Get the current value atomically
    }
}
