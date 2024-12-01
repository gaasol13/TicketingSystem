/**
 * BookingSimulation class simulates concurrent booking operations in a MySQL-based ticketing system.
 * It tests the system's ability to handle high concurrency, transaction management, and performance under load.
 */

package com.poortoys.examples.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.poortoys.examples.dao.EventDAO;
import com.poortoys.examples.dao.TicketDAO;
import com.poortoys.examples.dao.UserDAO;
import com.poortoys.examples.entities.User;
import com.poortoys.examples.entities.Event;
import com.poortoys.examples.entities.Booking;

public class BookingSimulation {
    // Configuration constants
    private static final int NUM_USERS = 5000; // Total number of simulated users
    private static final int MAX_TICKETS_PER_USER = 1; // Maximum tickets a user can book
    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors(); // Number of available CPU cores
    private static final int THREAD_POOL_SIZE = NUMBER_OF_CORES * 2; // Thread pool size for managing tasks
    private static final int SIMULATION_TIMEOUT_MINUTES = 3; // Simulation timeout in minutes

    // Simulation components
    private final BookingService bookingService; // Handles booking logic
    private final UserDAO userDAO; // DAO for retrieving user data
    private final EventDAO eventDAO; // DAO for retrieving event data
    private final TicketDAO ticketDAO; // DAO for ticket-related operations
    private final ExecutorService executorService; // Thread pool for concurrent tasks

    // Metrics for performance and booking results
    private long simulationStartTime; // Start time of the simulation
    private long simulationEndTime; // End time of the simulation
    private final AtomicInteger successfulBookings = new AtomicInteger(0); // Counter for successful bookings
    private final AtomicInteger failedBookings = new AtomicInteger(0); // Counter for failed bookings
    private int initialTicketCount; // Initial count of available tickets for the event
    private Event event; // The event being simulated

    /**
     * Constructor to initialize the simulation with required components.
     * @param bookingService Service handling booking operations
     * @param userDAO DAO for managing user data
     * @param eventDAO DAO for managing event data
     * @param ticketDAO DAO for managing ticket data
     */
    public BookingSimulation(BookingService bookingService, UserDAO userDAO, 
                             EventDAO eventDAO, TicketDAO ticketDAO) {
        this.bookingService = bookingService;
        this.userDAO = userDAO;
        this.eventDAO = eventDAO;
        this.ticketDAO = ticketDAO;
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE); // Initialize thread pool
    }

    /**
     * Runs the booking simulation for a specific event.
     * @param eventId ID of the event to simulate
     */
    public void runSimulation(int eventId) {
        try {
            initializeSimulation(eventId); // Initialize simulation components
            executeBookingTasks(eventId); // Execute booking tasks concurrently
            waitForCompletion(); // Wait for all tasks to finish
            printSimulationResults(eventId); // Display simulation results
        } catch (Exception e) {
            handleSimulationError(e); // Handle any errors during simulation
        } finally {
            cleanupResources(); // Ensure resources are properly cleaned up
        }
    }

    /**
     * Initializes the simulation by loading event details and tickets.
     * @param eventId ID of the event to simulate
     */
    private void initializeSimulation(int eventId) {
        System.out.println("\n=== Starting MySQL Booking Simulation ===");
        event = eventDAO.findById(eventId); // Retrieve event details
        if (event == null) {
            throw new RuntimeException("Event not found: " + eventId);
        }
        initialTicketCount = bookingService.getAvailableTicketSerials(eventId).size(); // Count available tickets
        System.out.println("Event: " + event.getEventName());
        System.out.println("Initial ticket count: " + initialTicketCount);
        simulationStartTime = System.nanoTime(); // Mark the start time of the simulation
    }

    /**
     * Executes concurrent booking tasks for the simulated users.
     * @param eventId ID of the event being simulated
     */
    private void executeBookingTasks(int eventId) {
        List<String> availableTickets = bookingService.getAvailableTicketSerials(eventId); // Retrieve available tickets
        int adjustedUsers = Math.min(NUM_USERS, availableTickets.size()); // Adjust user count based on ticket availability

        CountDownLatch completionLatch = new CountDownLatch(adjustedUsers); // Latch to track task completion
        List<User> users = userDAO.findAll(); // Load all users from the database
        Random random = new Random(); // Random generator for ticket and user selection

        for (int i = 0; i < adjustedUsers; i++) {
            executorService.submit(() -> { // Submit a task to the thread pool
                try {
                    executeBookingAttempt(users, availableTickets, random); // Perform a booking attempt
                } finally {
                    completionLatch.countDown(); // Signal task completion
                }
            });
        }

        try {
            // Wait for all tasks to complete or timeout
            if (!completionLatch.await(SIMULATION_TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
                System.err.println("Simulation timed out before completion");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupted status
        }
    }

    /**
     * Attempts to book tickets for a randomly selected user.
     * @param users List of users participating in the simulation
     * @param availableTickets List of currently available tickets
     * @param random Random generator for selection
     */
    private void executeBookingAttempt(List<User> users, List<String> availableTickets, Random random) {
        User user = users.get(random.nextInt(users.size())); // Select a random user
        int ticketsToBook = 1 + random.nextInt(MAX_TICKETS_PER_USER); // Determine tickets to book
        List<String> selectedTickets;

        synchronized (availableTickets) { // Ensure thread-safe ticket selection
            selectedTickets = selectRandomTickets(availableTickets, ticketsToBook);
        }

        if (!selectedTickets.isEmpty()) {
            try {
                Booking booking = bookingService.createBooking(user.getUserId(), selectedTickets, user.getEmail()); // Create booking
                if (booking != null) {
                    successfulBookings.incrementAndGet(); // Increment success counter
                } else {
                    failedBookings.incrementAndGet(); // Increment failure counter
                }
            } catch (Exception e) {
                failedBookings.incrementAndGet(); // Increment failure counter
                System.err.println("Booking failed for user " + user.getUserId() + ": " + e.getMessage());
            }
        } else {
            failedBookings.incrementAndGet(); // Increment failure counter if no tickets were selected
        }
    }

    /**
     * Randomly selects tickets for booking from the available pool.
     * @param availableTickets List of currently available tickets
     * @param count Number of tickets to select
     * @return List of selected ticket serials
     */
    private List<String> selectRandomTickets(List<String> availableTickets, int count) {
        List<String> selected = new ArrayList<>();
        Random random = new Random();
        synchronized (availableTickets) {
            for (int i = 0; i < count && !availableTickets.isEmpty(); i++) {
                int index = random.nextInt(availableTickets.size());
                selected.add(availableTickets.remove(index)); // Remove and select the ticket
            }
        }
        return selected;
    }

    /**
     * Waits for all booking tasks to complete.
     */
    private void waitForCompletion() {
        executorService.shutdown(); // Initiate shutdown of the thread pool
        try {
            executorService.awaitTermination(SIMULATION_TIMEOUT_MINUTES, TimeUnit.MINUTES); // Wait for termination
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
        simulationEndTime = System.nanoTime(); // Mark the end time of the simulation
    }

    /**
     * Prints the results of the simulation, including performance and booking metrics.
     * @param eventId ID of the simulated event
     */
    private void printSimulationResults(int eventId) {
        List<String> currentTickets = bookingService.getAvailableTicketSerials(eventId); // Fetch remaining tickets
        int totalBooked = initialTicketCount - currentTickets.size(); // Calculate total tickets booked

        System.out.println("\n=== Simulation Results ===");
        System.out.printf("Concurrent Users: %d%n", NUM_USERS);
        System.out.printf("Successful Bookings: %d%n", successfulBookings.get());
        System.out.printf("Failed Bookings: %d%n", failedBookings.get());
        System.out.printf("Total Tickets Booked: %d%n", totalBooked);
        System.out.printf("Remaining Tickets: %d%n", currentTickets.size());
        System.out.printf("Simulation Time: %d ms%n", (simulationEndTime - simulationStartTime) / 1_000_000);
    }

    /**
     * Handles errors that occur during the simulation.
     * @param e Exception that occurred
     */
    private void handleSimulationError(Exception e) {
        System.err.println("Simulation failed: " + e.getMessage());
        e.printStackTrace();
    }

    /**
     * Cleans up resources, including the thread pool.
     */
    private void cleanupResources() {
        if (!executorService.isShutdown()) {
            executorService.shutdownNow(); // Force shutdown of the thread pool
        }
    }
}
