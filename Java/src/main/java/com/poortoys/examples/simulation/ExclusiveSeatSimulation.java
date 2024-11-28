package com.poortoys.examples.simulation;

import java.util.*;
import java.util.concurrent.*;
import javax.persistence.EntityManager;

import com.poortoys.examples.dao.*;
import com.poortoys.examples.entities.*;

public class ExclusiveSeatSimulation {
    // Configuration Constants
    private static final int NUM_CONCURRENT_ASSIGNMENTS = 5;  // Lower number due to exclusive nature
    private static final int SIMULATION_TIMEOUT_MINUTES = 2;

    // Components
    private final EntityManager em;
    private final ExclusiveSeatAssignment seatAssignment;
    private final ExecutorService executorService;
    private final TicketDAO ticketDAO;
    private final BookingDAO bookingDAO;

    // Simulation state
    private long simulationStartTime;
    private long simulationEndTime;

    public ExclusiveSeatSimulation(EntityManager em, TicketDAO ticketDAO, BookingDAO bookingDAO) {
        this.em = em;
        this.ticketDAO = ticketDAO;
        this.bookingDAO = bookingDAO;
        this.seatAssignment = new ExclusiveSeatAssignment(em, ticketDAO, bookingDAO);
        this.executorService = Executors.newFixedThreadPool(NUM_CONCURRENT_ASSIGNMENTS);
    }

    public void runSimulation(int eventId) {
        try {
            initializeSimulation();
            executeAssignmentTasks(eventId);
            waitForCompletion();
            printSimulationResults();
        } catch (Exception e) {
            handleSimulationError(e);
        } finally {
            cleanupResources();
        }
    }

    private void initializeSimulation() {
        System.out.println("\n=== Starting Exclusive Seat Assignment Simulation ===");
        simulationStartTime = System.nanoTime();
    }

    private void executeAssignmentTasks(int eventId) {
        List<Booking> unassignedBookings = getUnassignedBookings(eventId);
        List<TicketCategory> categories = getEventCategories(eventId);
        
        CountDownLatch completionLatch = new CountDownLatch(NUM_CONCURRENT_ASSIGNMENTS);

        for (int i = 0; i < NUM_CONCURRENT_ASSIGNMENTS; i++) {
            executorService.submit(() -> {
                try {
                    processBookingAssignments(unassignedBookings, categories);
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        try {
            if (!completionLatch.await(SIMULATION_TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
                System.err.println("Simulation timed out before completion");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private List<Booking> getUnassignedBookings(int eventId) {
        return em.createQuery(
                "SELECT DISTINCT b FROM Booking b " +
                "JOIN b.bookingTickets bt " +
                "JOIN bt.ticket t " +
                "WHERE t.event.eventId = :eventId " +
                "AND t.status = :status " +
                "AND b.bookingStatus = :bookingStatus", 
                Booking.class)
            .setParameter("eventId", eventId)
            .setParameter("status", TicketStatus.SOLD)
            .setParameter("bookingStatus", BookingStatus.CONFIRMED)
            .getResultList();
    }

    private List<TicketCategory> getEventCategories(int eventId) {
        return em.createQuery(
                "SELECT tc FROM TicketCategory tc " +
                "WHERE tc.event.eventId = :eventId " +
                "AND tc.startDate <= :currentDate " +
                "AND (tc.endDate IS NULL OR tc.endDate >= :currentDate)", 
                TicketCategory.class)
            .setParameter("eventId", eventId)
            .setParameter("currentDate", new Date())
            .getResultList();
    }

    private void processBookingAssignments(List<Booking> bookings, List<TicketCategory> categories) {
        Random random = new Random();
        
        for (Booking booking : bookings) {
            TicketCategory category = categories.get(random.nextInt(categories.size()));
            String section = category.getArea();
            
            // Generate row and seat numbers
            String rowNumber = String.format("%02d", random.nextInt(20) + 1);
            String seatNumber = String.format("%03d", random.nextInt(30) + 1);
            
            // Get tickets from booking
            for (BookingTicket bt : booking.getBookingTickets()) {
                Ticket ticket = bt.getTicket();
                if (ticket.getSeatNumber() == null || ticket.getRowNumber() == null) {
                    boolean success = seatAssignment.assignSeats(
                        booking.getBookingId(),
                        section,
                        rowNumber,
                        seatNumber,
                        category.getTicketCategoryId()
                    );
                    
                    if (!success) {
                        System.out.println("Failed to assign seat for booking: " + 
                                         booking.getBookingId() + ", ticket: " + ticket.getTicketId());
                    }
                }
            }
        }
    }

    private void waitForCompletion() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(SIMULATION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        simulationEndTime = System.nanoTime();
    }

    private void printSimulationResults() {
        Map<String, Object> metrics = seatAssignment.getMetrics();
        
        System.out.println("\n=== Simulation Results ===");
        
        // Configuration metrics
        System.out.println("Configuration:");
        System.out.printf("Concurrent Assignment Threads: %d%n", NUM_CONCURRENT_ASSIGNMENTS);
        
        // Performance metrics
        System.out.println("\nPerformance Metrics:");
        long duration = (simulationEndTime - simulationStartTime) / 1_000_000;
        System.out.printf("Total Simulation Time: %d ms%n", duration);
        System.out.printf("Average Processing Time: %.2f ms%n", 
            metrics.get("average_processing_time_ms"));
        
        // Assignment metrics
        System.out.println("\nAssignment Results:");
        System.out.printf("Successful Assignments: %d%n", metrics.get("successful_assignments"));
        System.out.printf("Failed Assignments: %d%n", metrics.get("failed_assignments"));
        
        // Area breakdown
        System.out.println("\nArea Assignment Breakdown:");
        @SuppressWarnings("unchecked")
        Map<String, Integer> areaAssignments = (Map<String, Integer>) metrics.get("area_assignments");
        areaAssignments.forEach((area, count) -> 
            System.out.printf("%s: %d assignments%n", area, count));
        
        System.out.println("===============================\n");
    }

    private void handleSimulationError(Exception e) {
        System.err.println("Simulation failed: " + e.getMessage());
        e.printStackTrace();
    }

    private void cleanupResources() {
        if (!executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }
}