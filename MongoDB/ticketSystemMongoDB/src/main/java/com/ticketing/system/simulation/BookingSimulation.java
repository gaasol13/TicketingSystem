package com.ticketing.system.simulation;

import java.util.*;
import java.util.concurrent.*;
import org.bson.types.ObjectId;
import com.mongodb.client.MongoCollection;
import com.poortoys.examples.dao.*;
import com.ticketing.system.entities.*;
import com.ticketing.system.simulation.BookingService;
import dev.morphia.Datastore;

/**
 * Simulates concurrent booking operations in a MongoDB-based ticketing system.
 * Focuses on testing transaction consistency and performance under load.
 */
public class BookingSimulation {
    // Configuration Constants
    private static final int NUM_USERS = 10;
    private static final int MAX_TICKETS_PER_USER = 1;
    private static final int THREAD_POOL_SIZE = 10;
    private static final int SIMULATION_TIMEOUT_MINUTES = 1;

    // Simulation components
    private final BookingService bookingService;
    private final UserDAO userDAO;
    private final EventDAO eventDAO;
    private final TicketDAO ticketDAO;
    private final Datastore datastore;
    private final ExecutorService executorService;

    // Simulation state
    private long simulationStartTime;
    private long simulationEndTime;
    private long initialTicketCount;
    private Event event;

    public BookingSimulation(Datastore datastore, BookingDAO bookingDAO, 
                           UserDAO userDAO, EventDAO eventDAO, TicketDAO ticketDAO) {
        this.datastore = datastore;
        this.userDAO = userDAO;
        this.eventDAO = eventDAO;
        this.ticketDAO = ticketDAO;
        this.bookingService = new BookingService(bookingDAO, ticketDAO, userDAO, 
                                               eventDAO, datastore);
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    /**
     * Runs the concurrent booking simulation for a specific event.
     */
    public void runSimulation(ObjectId eventId) {
        try {
            initializeSimulation(eventId);
            executeBookingTasks(eventId);
            waitForCompletion();
            validateFinalState(eventId);
            printSimulationResults(eventId);
        } catch (Exception e) {
            handleSimulationError(e);
        } finally {
            cleanupResources();
        }
    }

    private void initializeSimulation(ObjectId eventId) {
        System.out.println("\n=== Starting MongoDB Booking Simulation ===");
        
        event = eventDAO.findById(eventId);
        if (event == null) {
            throw new RuntimeException("Event not found: " + eventId);
        }

        initialTicketCount = ticketDAO.countAvailableTickets(eventId);
        System.out.println("Event: " + event.getName());
        System.out.println("Initial ticket count: " + initialTicketCount);
        simulationStartTime = System.nanoTime();
    }

    private void executeBookingTasks(ObjectId eventId) {
        List<Callable<Boolean>> tasks = new ArrayList<>();
        List<User> users = userDAO.findAll();

        if (users.size() < NUM_USERS) {
            throw new IllegalStateException("Not enough users for simulation. " + 
                "Required: " + NUM_USERS + ", Available: " + users.size());
        }

        Random random = new Random();
        for (int i = 0; i < NUM_USERS; i++) {
            final User user = users.get(random.nextInt(users.size()));
            final int ticketsToBook = random.nextInt(MAX_TICKETS_PER_USER) + 1;

            tasks.add(() -> bookingService.bookTickets(user.getId(), eventId, ticketsToBook));
        }

        try {
            List<Future<Boolean>> results = executorService.invokeAll(tasks);
            processBookingResults(results);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Booking tasks interrupted: " + e.getMessage());
        }
    }

    private void processBookingResults(List<Future<Boolean>> results) {
        int successful = 0;
        int failed = 0;

        for (Future<Boolean> result : results) {
            try {
                if (result.get(SIMULATION_TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
                    successful++;
                } else {
                    failed++;
                }
            } catch (Exception e) {
                failed++;
                System.err.println("Error processing booking result: " + e.getMessage());
            }
        }

        System.out.printf("Processed results - Successful: %d, Failed: %d%n", 
            successful, failed);
    }

    private void waitForCompletion() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(SIMULATION_TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
                System.err.println("Simulation timed out before completion");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        simulationEndTime = System.nanoTime();
    }

    private void validateFinalState(ObjectId eventId) {
        Map<String, Object> finalState = bookingService.validateDatabaseState(eventId);
        System.out.println("\nFinal State Validation:");
        finalState.forEach((key, value) -> 
            System.out.printf("%s: %s%n", key, value));
    }

    private void printSimulationResults(ObjectId eventId) {
        BookingService.BookingMetrics metrics = bookingService.getDetailedMetrics();
        long currentAvailable = ticketDAO.countAvailableTickets(eventId);
        
        System.out.println("\n=== Simulation Results ===");

        // Configuration metrics
        System.out.println("Configuration:");
        System.out.printf("Concurrent Users: %d%n", NUM_USERS);
        System.out.printf("Max Tickets Per User: %d%n", MAX_TICKETS_PER_USER);
        System.out.printf("Thread Pool Size: %d%n", THREAD_POOL_SIZE);

        // Event details
        System.out.println("\nEvent Details:");
        System.out.printf("Event: %s%n", event.getName());
        System.out.printf("Venue: %s%n", event.getVenue().getVenueName());

        // Performance metrics
        System.out.println("\nPerformance Metrics:");
        long duration = (simulationEndTime - simulationStartTime) / 1_000_000;
        System.out.printf("Total Simulation Time: %d ms%n", duration);
        System.out.printf("Average Query Time: %.2f ms%n", metrics.getAverageQueryTime());
        System.out.printf("Total Queries: %d%n", metrics.getTotalQueries());

        // Booking metrics
        System.out.println("\nBooking Results:");
        System.out.printf("Successful Bookings: %d%n", metrics.getSuccessfulBookings());
        System.out.printf("Failed Bookings: %d%n", metrics.getFailedBookings());
        System.out.printf("Total Attempts: %d%n", 
            metrics.getSuccessfulBookings() + metrics.getFailedBookings());
        System.out.printf("Success Rate: %.2f%%%n", 
            (double)metrics.getSuccessfulBookings() / NUM_USERS * 100);

        // Inventory metrics
        System.out.println("\nInventory Status:");
        System.out.printf("Initial Available Tickets: %d%n", initialTicketCount);
        System.out.printf("Total Tickets Booked: %d%n", 
            metrics.getTotalTicketsBooked());
        System.out.printf("Remaining Available: %d%n", currentAvailable);
        
        // Verify consistency
        boolean isConsistent = (initialTicketCount - metrics.getTotalTicketsBooked()) 
            == currentAvailable;
        System.out.printf("\nData Consistency Check: %s%n", 
            isConsistent ? "PASSED" : "FAILED");

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

    /**
     * Retrieves simulation statistics for analysis
     */
    public SimulationStats getSimulationStats() {
        return new SimulationStats(
            simulationStartTime,
            simulationEndTime,
            initialTicketCount,
            bookingService.getDetailedMetrics()
        );
    }

    /**
     * Inner class to hold simulation statistics
     */
    public static class SimulationStats {
        private final long startTime;
        private final long endTime;
        private final long initialTickets;
        private final BookingService.BookingMetrics bookingMetrics;

        public SimulationStats(long startTime, long endTime, long initialTickets,
                             BookingService.BookingMetrics bookingMetrics) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.initialTickets = initialTickets;
            this.bookingMetrics = bookingMetrics;
        }

        public long getDurationMs() {
            return (endTime - startTime) / 1_000_000;
        }

        public double getSuccessRate() {
            int total = bookingMetrics.getSuccessfulBookings() + 
                       bookingMetrics.getFailedBookings();
            return total > 0 ? 
                (double) bookingMetrics.getSuccessfulBookings() / total * 100 : 0;
        }

        public long getRemainingTickets() {
            return initialTickets - bookingMetrics.getTotalTicketsBooked();
        }

        public BookingService.BookingMetrics getBookingMetrics() {
            return bookingMetrics;
        }

        @Override
        public String toString() {
            return String.format(
                "SimulationStats{duration=%dms, successRate=%.2f%%, " +
                "initialTickets=%d, remainingTickets=%d, metrics=%s}",
                getDurationMs(), getSuccessRate(), initialTickets, 
                getRemainingTickets(), bookingMetrics
            );
        }
    }
}