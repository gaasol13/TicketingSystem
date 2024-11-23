package com.poortoys.examples.simulation; // Package declaration, like a folder for organizing the code

// Necessary imports for the service functionality
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch; // For synchronizing threads
import java.util.concurrent.ExecutorService; // For managing a pool of threads
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger; // For thread-safe integer operations

import com.poortoys.examples.dao.UserDAO; // Custom data access object for users
import com.poortoys.examples.entities.User; // User entity class

/**
 * Class responsible for simulating the booking process in a ticketing system.
 * It tests both positive and negative scenarios to evaluate MySQL's capabilities
 * in handling transaction consistency and schema modifications under concurrent operations.
 */
public class BookingSimulation {
    
    // Simulation configuration constants
    // Standardized configuration
    protected static final int NUM_USERS = 100;               
    protected static final int MAX_TICKETS_PER_USER = 2;      
    protected static final int THREAD_POOL_SIZE = 10;         
    protected static final int SIMULATION_TIMEOUT_MINUTES = 5; 
    protected static final int BATCH_SIZE = 100;
    
    // Standardized metrics across both implementations
    protected long simulationStartTime;
    protected long simulationEndTime;
    protected long totalQueryTime = 0;
    protected int totalQueries = 0;
    // Dependencies for the simulation
    private final BookingService bookingService;              // Service that handles booking operations
    private final UserDAO userDAO;                            // Data access object for user-related database operations
    private final ExecutorService executorService;            // Executor service to manage concurrent tasks (threads)

    // Atomic counters for tracking simulation metrics
    private final AtomicInteger successfulBookings = new AtomicInteger(0);   // Counts successful bookings
    private final AtomicInteger failedBookings = new AtomicInteger(0);       // Counts failed bookings
    private final AtomicInteger concurrencyConflicts = new AtomicInteger(0); // Counts concurrency conflicts (not used in code)
    private long schemaModificationTime = 0;                               // Time taken to modify the schema (not used in code)

    /**
     * Constructor for BookingSimulation.
     * Initializes the BookingService, UserDAO, and ExecutorService.
     * 
     * @param bookingService Service handling booking operations
     * @param userDAO        DAO for user-related operations
     */
    public BookingSimulation(BookingService bookingService, UserDAO userDAO) {
        this.bookingService = bookingService; // Assigns the booking service
        this.userDAO = userDAO;               // Assigns the user DAO
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE); // Creates a thread pool with a fixed number of threads
    }

    /**
     * Runs the full simulation encompassing both positive and negative scenarios.
     * 
     * @param eventId ID of the event for which the simulation is run
     */
    public void runFullSimulation(int eventId) {
        try {
            // Retrieve the initial count of available tickets for the event
            int initialTicketCount = bookingService.getAvailableTicketSerials(eventId).size();
            System.out.println("\n=== Starting MySQL Simulation ===");
            System.out.println("Event: Jazz Nights"); // Just an example event name
            System.out.println("Initial ticket count: " + initialTicketCount); // Prints how many tickets are available at the start

            // Execute the positive scenario to test transaction consistency
            runPositiveScenario(eventId);

            // Execute the negative scenario to test schema modification under concurrency
            runNegativeScenario();

            // Print the final metrics after the simulation
            printFinalMetrics(eventId, initialTicketCount);
        } catch (Exception e) {
            // If something goes wrong during the simulation, print an error message
            System.err.println("Simulation failed: " + e.getMessage());
        } finally {
            // Ensure the executor service is properly shut down to free resources
            executorService.shutdown();
        }
    }

    /**
     * Runs the positive scenario of the simulation, which tests transaction consistency
     * by simulating multiple users attempting to book tickets concurrently.
     * 
     * @param eventId ID of the event for which tickets are being booked
     */
    private void runPositiveScenario(int eventId) {
        System.out.println("Running transaction consistency scenario...");

        CountDownLatch startLatch = new CountDownLatch(1); // Used to make sure all threads start at the same time
        CountDownLatch completionLatch = new CountDownLatch(NUM_USERS); // Used to wait for all threads to finish

        // Get available tickets for the event
        List<String> availableTickets = bookingService.getAvailableTicketSerials(eventId);
        System.out.println("Found " + availableTickets.size() + " available tickets"); // Prints how many tickets are available

        // Get all users from the database
        List<User> users = userDAO.findAll();
        System.out.println("Found " + users.size() + " users"); // Prints how many users are in the system

        Random random = new Random(); // Random number generator

        // Submit booking tasks for each user
        for (int i = 0; i < NUM_USERS; i++) {
            executorService.submit(() -> { // Submits a new task to the thread pool
                try {
                    startLatch.await(); // Wait until startLatch is counted down to 0 (so all threads start together)

                    // Select a random user from the list
                    User user = users.get(random.nextInt(users.size()));
                    System.out.println("Selected user: " + user.getUserName()); // Prints the selected user's name

                    // Decide how many tickets the user will try to book (1 or 2)
                    int ticketsToBook = 1 + random.nextInt(MAX_TICKETS_PER_USER);
                    List<String> selectedTickets;

                    // Synchronize this block to make sure only one thread accesses availableTickets at a time
                    synchronized(this) {
                        selectedTickets = selectRandomTickets(availableTickets, ticketsToBook); // Select random tickets for the user
                    }

                    System.out.println("User " + user.getUserName() + " attempting to book " + 
                                     selectedTickets.size() + " tickets: " + selectedTickets);

                    try {
                        // Attempt to create a booking for the user
                        bookingService.createBooking(user.getUserId(), selectedTickets, user.getEmail());
                        recordSuccess(); // If successful, record the success
                        System.out.println("Booking successful for user: " + user.getUserName());
                    } catch (Exception e) {
                        recordFailure(); // If there's an exception, record the failure
                        System.err.println("Booking failed for user " + user.getUserName() + ": " + e.getMessage());
                    }
                } catch (Exception e) {
                    failedBookings.incrementAndGet(); // If the task itself fails, increment failed bookings
                    System.err.println("Task execution failed: " + e.getMessage());
                } finally {
                    completionLatch.countDown(); // Decrease the completion latch count
                }
            });
        }

        startLatch.countDown(); // Start all threads at the same time by counting down the startLatch

        try {
            // Wait for all tasks to complete or until the simulation duration expires
            completionLatch.await(SIMULATION_DURATION_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupt status
        }
    }

    /**
     * Runs the negative scenario of the simulation, which tests the system's ability
     * to handle schema modifications under concurrent operations.
     */
    private void runNegativeScenario() {
        System.out.println("Running schema modification scenario...");

        try {
            bookingService.modifySchema("drop_user_columns"); // Attempt to modify the database schema
            System.out.println("Successfully removed confirmation columns from users table");
        } catch (Exception e) {
            System.err.println("Failed to modify users table: " + e.getMessage()); // Print error if schema modification fails
        }
    }

    /**
     * Selects a random subset of tickets from the available tickets list.
     * We need to make sure this is thread-safe.
     * 
     * @param availableTickets List of available tickets
     * @param count            Number of tickets to select
     * @return                 List of selected tickets
     */
    private List<String> selectRandomTickets(List<String> availableTickets, int count) {
        List<String> selected = new ArrayList<>(); // List to hold selected tickets
        synchronized (availableTickets) { // Synchronize on availableTickets to prevent concurrent modifications
            for (int i = 0; i < count && !availableTickets.isEmpty(); i++) {
                int index = new Random().nextInt(availableTickets.size()); // Select a random index
                selected.add(availableTickets.remove(index)); // Remove the ticket from availableTickets and add it to selected
            }
        }
        return selected; // Return the list of selected tickets
    }

    /**
     * Records a successful booking by incrementing the corresponding counter.
     */
    private void recordSuccess() {
        successfulBookings.incrementAndGet(); // Increment the successful bookings counter atomically
    }

    /**
     * Records a failed booking by incrementing the corresponding counter.
     */
    private void recordFailure() {
        failedBookings.incrementAndGet(); // Increment the failed bookings counter atomically
    }

    /**
     * Prints the final metrics and results of the simulation, including the number of
     * successful and failed bookings, concurrency conflicts, and schema modification time.
     * 
     * @param eventId            ID of the event
     * @param initialTicketCount Number of tickets available at the start
     */
    private void printFinalMetrics(int eventId, int initialTicketCount) {
        // Get the current list of available ticket serials after the simulation
        List<String> currentTickets = bookingService.getAvailableTicketSerials(eventId);
        // Calculate the total number of tickets booked during the simulation
        int totalTicketsBooked = initialTicketCount - currentTickets.size();

        System.out.println("\n=== MySQL Simulation Results ===");
        System.out.println("Event: Jazz Nights");
        System.out.println("Concurrent Users: " + NUM_USERS);
        System.out.println("Total booking attempts: " + NUM_USERS);
        System.out.println("Total tickets available before booking: " + initialTicketCount);
        System.out.println("Total tickets booked: " + totalTicketsBooked);
        System.out.println("Successful bookings: " + successfulBookings.get());
        System.out.println("Failed bookings: " + failedBookings.get());
        System.out.println("Tickets remaining: " + currentTickets.size());
        System.out.println("=======================\n");
    }
}
