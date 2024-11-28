package com.poortoys.examples.simulation;

import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;

import com.poortoys.examples.dao.*;
import com.poortoys.examples.entities.*;

public class ExclusiveSeatAssignment {
    private final EntityManager em;
    private final TicketDAO ticketDAO;
    private final BookingDAO bookingDAO;

    // Metrics tracking
    private final AtomicInteger successfulAssignments = new AtomicInteger(0);
    private final AtomicInteger failedAssignments = new AtomicInteger(0);
    private final Map<String, Integer> areaAssignments = new ConcurrentHashMap<>();
    private long totalProcessingTime = 0;
    private int totalOperations = 0;

    public ExclusiveSeatAssignment(EntityManager em, TicketDAO ticketDAO, BookingDAO bookingDAO) {
        this.em = em;
        this.ticketDAO = ticketDAO;
        this.bookingDAO = bookingDAO;
    }

    /**
     * Assigns seats for a booking within a specific ticket category
     */
    public boolean assignSeats(int bookingId, String area, String rowNumber, 
                             String seatNumber, int ticketCategoryId) {
        long startTime = System.nanoTime();
        
        try {
            em.getTransaction().begin();

            // Validate ticket category
            TicketCategory category = validateTicketCategory(ticketCategoryId);
            if (!isValidAreaForCategory(category, area)) {
                throw new IllegalArgumentException(
                    "Area " + area + " not valid for category " + category.getDescription());
            }

            // Lock and validate the booking
            Booking booking = findAndLockBooking(bookingId);
            if (booking == null) {
                throw new IllegalArgumentException("Booking not found: " + bookingId);
            }

            // Verify seat availability
            if (!isSeatAvailable(area, rowNumber, seatNumber)) {
                return false;
            }

            // Update ticket with seat assignment
            updateTicketAssignment(booking, category, area, rowNumber, seatNumber);

            em.getTransaction().commit();
            updateMetrics(true, area);
            return true;

        } catch (Exception e) {
            handleAssignmentError(e);
            return false;
        } finally {
            cleanupTransaction();
            recordProcessingTime(startTime);
        }
    }

    private TicketCategory validateTicketCategory(int ticketCategoryId) {
        TicketCategory category = em.find(TicketCategory.class, ticketCategoryId);
        if (category == null) {
            throw new IllegalArgumentException("Invalid ticket category: " + ticketCategoryId);
        }

        Date now = new Date();
        if (now.before(category.getStartDate()) || 
            (category.getEndDate() != null && now.after(category.getEndDate()))) {
            throw new IllegalStateException("Ticket category not active at this time");
        }

        return category;
    }

    private boolean isValidAreaForCategory(TicketCategory category, String area) {
        return category.getArea().equalsIgnoreCase(area);
    }

    private Booking findAndLockBooking(int bookingId) {
        try {
            return em.createQuery(
                    "SELECT b FROM Booking b WHERE b.bookingId = :id", 
                    Booking.class)
                .setParameter("id", bookingId)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private boolean isSeatAvailable(String area, String rowNumber, String seatNumber) {
        Long count = em.createQuery(
                "SELECT COUNT(t) FROM Ticket t " +
                "WHERE t.ticketCategory.area = :area " +
                "AND t.rowNumber = :rowNumber " +
                "AND t.seatNumber = :seatNumber " +
                "AND t.status = :status", Long.class)
            .setParameter("area", area)
            .setParameter("rowNumber", rowNumber)
            .setParameter("seatNumber", seatNumber)
            .setParameter("status", TicketStatus.SOLD)
            .getSingleResult();
        
        return count == 0;
    }

    private void updateTicketAssignment(Booking booking, TicketCategory category, 
                                      String area, String rowNumber, String seatNumber) {
        // Find an available ticket for this booking
        Ticket ticket = em.createQuery(
                "SELECT t FROM Ticket t " +
                "WHERE t.booking = :booking " +
                "AND t.ticketCategory = :category " +
                "AND t.status = :status", Ticket.class)
            .setParameter("booking", booking)
            .setParameter("category", category)
            .setParameter("status", TicketStatus.SOLD)
            .setMaxResults(1)
            .getSingleResult();

        // Update ticket with seat information
        ticket.setRowNumber(rowNumber);
        ticket.setSeatNumber(seatNumber);
        em.merge(ticket);
    }

    private void handleAssignmentError(Exception e) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        updateMetrics(false, null);
        System.err.println("Error during seat assignment: " + e.getMessage());
    }

    private void cleanupTransaction() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }

    private void updateMetrics(boolean success, String area) {
        if (success) {
            successfulAssignments.incrementAndGet();
            if (area != null) {
                areaAssignments.merge(area, 1, Integer::sum);
            }
        } else {
            failedAssignments.incrementAndGet();
        }
    }

    private void recordProcessingTime(long startTime) {
        totalProcessingTime += (System.nanoTime() - startTime);
        totalOperations++;
    }

    /**
     * Retrieves metrics about seat assignments
     */
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        metrics.put("successful_assignments", successfulAssignments.get());
        metrics.put("failed_assignments", failedAssignments.get());
        metrics.put("average_processing_time_ms", 
            totalOperations > 0 ? totalProcessingTime / totalOperations / 1_000_000.0 : 0);
        metrics.put("area_assignments", new HashMap<>(areaAssignments));
        return metrics;
    }
}


/*
 * package com.poortoys.examples;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.poortoys.examples.dao.*;
import com.poortoys.examples.simulation.*;

public class AppMain {
    public static void main(String[] args) {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        
        try {
            System.out.println("Starting Exclusive Seat Assignment Simulation...");
            emf = Persistence.createEntityManagerFactory("ticketingsystem");
            em = emf.createEntityManager();
            
            // Initialize DAOs
            TicketDAO ticketDAO = new TicketDAO(em);
            BookingDAO bookingDAO = new BookingDAO(em);
            
            // Create and run simulation
            ExclusiveSeatSimulation simulation = new ExclusiveSeatSimulation(
                em, ticketDAO, bookingDAO
            );
            
            // Run simulation for event ID 2
            simulation.runSimulation(3);
            
            System.out.println("\nSimulation completed. Press Enter to exit...");
            System.in.read();
            
        } catch (Exception e) {
            System.err.println("Error during execution:");
            e.printStackTrace();
        } finally {
            // Clean up resources
            if (em != null && em.isOpen()) {
                em.close();
            }
            if (emf != null && emf.isOpen()) {
                emf.close();
            }
        }
    }
}


/*
 * I'd like to know the
 * 
 * 
 * I'd like to standardize my AppMain to work with both scenarios and enhance the displayed metrics (in the simulation class) with the following information:


Event Name: Instead of the event ID, display the actual event name.

Venue: Show the venue where the event will take place.

Total Tickets by Category: Display the total number of tickets available for each category of the event.

Total Tickets Booked by Category: Show the number of tickets already booked for each category.

Total Tickets Available: Display the total number of tickets still available across all categories.

Total Ticket Categories:
 */
 