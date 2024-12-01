// This Java program simulates a booking system using MongoDB with concurrent user operations
// It focuses on testing transaction consistency and performance under heavy load

package com.ticketing.system.simulation;

// Importing required classes for database interaction, concurrency, and utilities
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.bson.types.ObjectId;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.poortoys.examples.dao.*;
import com.ticketing.system.entities.*;
import com.ticketing.system.simulation.BookingService;
import dev.morphia.Datastore;

/**
 * Class representing a booking simulation in a MongoDB-based ticketing system.
 * Simulates concurrent booking attempts, measures system performance, and checks data consistency.
 */
public class BookingSimulation {

    // Configuration constants
    private static final int NUM_USERS = 100; // Number of simulated users
    private static final int MAX_TICKETS_PER_USER = 1; // Max tickets a single user can book
    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors(); // Number of CPU cores available
    private static final int THREAD_POOL_SIZE = NUMBER_OF_CORES * 2; // Thread pool size for managing tasks
    private static final int SIMULATION_TIMEOUT_MINUTES = 1; // Time limit for simulation

    // Dependencies and shared resources
    private final BookingService bookingService; // Handles booking logic and database interaction
    private final UserDAO userDAO; // Data Access Object for managing user data
    private final EventDAO eventDAO; // DAO for managing event data
    private final TicketDAO ticketDAO; // DAO for managing ticket data
    private final Datastore datastore; // Datastore for MongoDB interaction
    private final ExecutorService executorService; // Manages threads for concurrent booking tasks

    // Simulation state
    private long simulationStartTime; // Start time of the simulation
    private long simulationEndTime; // End time of the simulation
    private long initialTicketCount; // Initial count of available tickets for the event
    private Event event; // Event being simulated

    /**
     * Constructor for initializing the simulation with required dependencies.
     */
    public BookingSimulation(Datastore datastore, BookingDAO bookingDAO, UserDAO userDAO, 
                             EventDAO eventDAO, TicketDAO ticketDAO) {
        this.datastore = datastore;
        this.userDAO = userDAO;
        this.eventDAO = eventDAO;
        this.ticketDAO = ticketDAO;
        this.bookingService = new BookingService(bookingDAO, ticketDAO, userDAO, eventDAO, datastore);
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE); // Creating a thread pool
    }

    /**
     * Starts the simulation with a specific event.
     */
    public void runSimulation(ClientSession session, ObjectId eventId) {
        try {
            // Initialize the simulation state
            initializeSimulation(session, eventId);

            // Execute booking tasks concurrently
            executeBookingTasks(session, eventId);

            // Wait for all tasks to complete
            waitForCompletion();

            // Print the simulation results
            printSimulationResults(session, eventId);
        } catch (Exception e) {
            handleSimulationError(e); // Handle exceptions during the simulation
        } finally {
            cleanupResources(); // Clean up resources such as threads
        }
    }

    /**
     * Initializes the simulation state by loading the event and initial ticket count.
     */
    private void initializeSimulation(ClientSession session, ObjectId eventId) {
        System.out.println("\n=== Starting MongoDB Booking Simulation ===");
        
        // Fetch event details
        event = eventDAO.findById(eventId);
        if (event == null) {
            throw new RuntimeException("Event not found: " + eventId);
        }

        // Retrieve the initial number of available tickets
        initialTicketCount = ticketDAO.countAvailableTickets(session, eventId);
        System.out.println("Event: " + event.getName());
        System.out.println("Initial ticket count: " + initialTicketCount);

        // Mark the start time of the simulation
        simulationStartTime = System.nanoTime();
    }

    /**
     * Executes booking tasks concurrently for the simulated users.
     */
    private void executeBookingTasks(ClientSession session, ObjectId eventId) {
        long availableTickets = ticketDAO.countAvailableTickets(session, eventId);
        int adjustedUsers = Math.min(NUM_USERS, (int) availableTickets); // Adjust user count to available tickets
        
        CountDownLatch completionLatch = new CountDownLatch(adjustedUsers); // Latch to track task completion
        ConcurrentHashMap<String, AtomicInteger> resultTracker = new ConcurrentHashMap<>(); // Tracks booking outcomes
        
        List<User> users = userDAO.findAll(); // Load all users from the database
        Random random = new Random(); // Random object for selecting users

        // Loop to submit booking tasks for users
        for (int i = 0; i < adjustedUsers; i++) {
            final User user = users.get(random.nextInt(users.size())); // Select a random user
            executorService.submit(() -> { // Submit a task to the thread pool
                try {
                    // Attempt to book tickets for the user
                    boolean success = bookingService.bookTickets(user.getId(), eventId, 1);

                    // Update the result tracker based on the outcome
                    resultTracker.computeIfAbsent(success ? "successful" : "failed", 
                        k -> new AtomicInteger()).incrementAndGet();
                } finally {
                    completionLatch.countDown(); // Signal task completion
                }
            });
        }

        // Wait for all tasks to complete or timeout
        try {
            completionLatch.await(SIMULATION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Waits for all threads in the executor service to finish.
     */
    private void waitForCompletion() {
        executorService.shutdown(); // Initiate shutdown of the thread pool
        try {
            // Wait for all threads to terminate or timeout
            if (!executorService.awaitTermination(SIMULATION_TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
                System.err.println("Simulation timed out before completion");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupt status if interrupted
        }
        simulationEndTime = System.nanoTime(); // Mark the end time of the simulation
    }

    /**
     * Prints the simulation results, including performance and booking metrics.
     */
    private void printSimulationResults(ClientSession session, ObjectId eventId) {
        BookingService.BookingMetrics metrics = bookingService.getDetailedMetrics(); // Get booking metrics
        long currentAvailable = ticketDAO.countAvailableTickets(session, eventId); // Current ticket availability
        
        System.out.println("\n=== Simulation Results ===");

        // Display configuration and performance details
        System.out.printf("Total Simulation Time: %d ms%n", 
            (simulationEndTime - simulationStartTime) / 1_000_000);
        System.out.printf("Successful Bookings: %d%n", metrics.getSuccessfulBookings());
        System.out.printf("Failed Bookings: %d%n", metrics.getFailedBookings());
        System.out.printf("Initial Available Tickets: %d%n", initialTicketCount);
        System.out.printf("Remaining Available: %d%n", currentAvailable);

        // Check data consistency by comparing expected and actual ticket counts
        boolean isConsistent = (initialTicketCount - metrics.getTotalTicketsBooked()) == currentAvailable;
        System.out.printf("Data Consistency Check: %s%n", isConsistent ? "PASSED" : "FAILED");
    }

    /**
     * Handles any errors that occur during the simulation.
     */
    private void handleSimulationError(Exception e) {
        System.err.println("Simulation failed: " + e.getMessage());
        e.printStackTrace(); // Print the exception stack trace
    }

    /**
     * Cleans up resources such as shutting down the executor service.
     */
    private void cleanupResources() {
        if (!executorService.isShutdown()) {
            executorService.shutdownNow(); // Force shutdown if not already done
        }
    }
}
