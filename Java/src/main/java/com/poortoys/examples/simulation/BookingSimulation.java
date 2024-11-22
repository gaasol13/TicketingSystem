package com.poortoys.examples.simulation; // I hope this is correct

// necessary imports for the service functionality
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.poortoys.examples.dao.UserDAO;
import com.poortoys.examples.entities.User;

/**
 * class responsible for simulating the booking process in a ticketing system
 * it tests both positive and negative scenarios to evaluate MySQL's capabilities
 * in handling transaction consistency and schema modifications under concurrent operations
 */
public class BookingSimulation {
    
    // simulation configuration constants
    private static final int NUM_USERS = 5;                  // number of users to simulate
    private static final int MAX_TICKETS_PER_USER = 2;       // maximum number of tickets a user can book
    private static final int THREAD_POOL_SIZE = 5;           // number of threads in the thread pool
    private static final int SIMULATION_DURATION_SECONDS = 30; // duration for which the simulation runs
    
    // dependencies for the simulation
    private final BookingService bookingService;              // service handling booking operations
    private final UserDAO userDAO;                            // dao for user-related database operations
    private final ExecutorService executorService;            // executor service for managing concurrent tasks
    
    // atomic counters for tracking simulation metrics
    private final AtomicInteger successfulBookings = new AtomicInteger(0);   // counter for successful bookings
    private final AtomicInteger failedBookings = new AtomicInteger(0);       // counter for failed bookings
    private final AtomicInteger concurrencyConflicts = new AtomicInteger(0); // counter for concurrency conflicts
    private long schemaModificationTime = 0;                               // time taken to modify the schema
    
    /**
     * constructor for BookingSimulation
     * initializes the BookingService, UserDAO, and ExecutorService
     * 
     * @param bookingService service handling booking operations
     * @param userDAO        dao for user-related operations
     */
    public BookingSimulation(BookingService bookingService, UserDAO userDAO) {
        this.bookingService = bookingService;
        this.userDAO = userDAO;
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE); // initialize a fixed thread pool
    }

    /**
     * runs the full simulation encompassing both positive and negative scenarios
     * 
     * @param eventId ID of the event for which the simulation is run
     */
    public void runFullSimulation(int eventId) {
        try {
            // retrieve the initial count of available tickets for the event
            int initialTicketCount = bookingService.getAvailableTicketSerials(eventId).size();
            System.out.println("\n=== Starting MySQL Simulation ===");
            System.out.println("Event: Jazz Nights");
            System.out.println("Initial ticket count: " + initialTicketCount);
    
            // execute the positive scenario to test transaction consistency
            runPositiveScenario(eventId);
    
            // execute the negative scenario to test schema modification under concurrency
            runNegativeScenario();
    
            // print the final metrics after the simulation
            printFinalMetrics(eventId, initialTicketCount);
        } catch (Exception e) {
            // oops, something went wrong during the simulation
            System.err.println("Simulation failed: " + e.getMessage());
        } finally {
            // make sure the executor service is properly shut down to free resources
            executorService.shutdown();
        }
    }

    /**
     * runs the positive scenario of the simulation, which tests transaction consistency
     * by simulating multiple users attempting to book tickets concurrently
     * 
     * @param eventId ID of the event for which tickets are being booked
     */
    private void runPositiveScenario(int eventId) {
        System.out.println("Running transaction consistency scenario...");
    
        // latch to synchronize the start of all booking tasks
        CountDownLatch startLatch = new CountDownLatch(1);
        // latch to wait for all booking tasks to complete
        CountDownLatch completionLatch = new CountDownLatch(NUM_USERS);
    
        // get the list of available ticket serials for the event
        List<String> availableTickets = bookingService.getAvailableTicketSerials(eventId);
        // get all users from the database
        List<User> users = userDAO.findAll();
        Random random = new Random(); // random number generator for selecting users and tickets
    
        // submit booking tasks for each user
        for (int i = 0; i < NUM_USERS; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await(); // wait for the signal to start
                    long startTime = System.currentTimeMillis(); // record the start time of the booking attempt
    
                    // pick a random user from the list
                    User user = users.get(random.nextInt(users.size()));
                    // decide the number of tickets to book (1 to MAX_TICKETS_PER_USER)
                    int ticketsToBook = 1 + random.nextInt(MAX_TICKETS_PER_USER);
                    // select random tickets from the available list
                    List<String> selectedTickets = selectRandomTickets(availableTickets, ticketsToBook);
    
                    try {
                        // try to create a booking with the selected tickets
                        bookingService.createBooking(user.getUserId(), selectedTickets, user.getEmail());
                        recordSuccess(); // record a successful booking
                    } catch (Exception e) {
                        recordFailure(); // record a failed booking
                    }
                } catch (Exception e) {
                    failedBookings.incrementAndGet(); // increment failed bookings in case of unexpected exceptions
                } finally {
                    completionLatch.countDown(); // signal that the booking task has completed
                }
            });
        }
    
        startLatch.countDown(); // signal all booking tasks to start
    
        try {
            // wait for all booking tasks to complete or timeout after SIMULATION_DURATION_SECONDS
            completionLatch.await(SIMULATION_DURATION_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // restore the interrupted status
        }
    }

    /**
     * runs the negative scenario of the simulation, which tests the system's ability
     * to handle schema modifications under concurrent operations
     */
    private void runNegativeScenario() {
        System.out.println("Running schema modification scenario...");
    
        try {
            bookingService.modifySchema("drop_user_columns");
            System.out.println("Successfully removed confirmation columns from users table");
        } catch (Exception e) {
            System.err.println("Failed to modify users table: " + e.getMessage());
        }
    }

    /**
     * selects a random subset of tickets from the available tickets list
     * I need to make sure this is thread-safe
     */
    private List<String> selectRandomTickets(List<String> availableTickets, int count) {
        List<String> selected = new ArrayList<>(); // list to hold selected tickets
        synchronized (availableTickets) { // synchronize to prevent concurrent modifications
            for (int i = 0; i < count && !availableTickets.isEmpty(); i++) {
                int index = new Random().nextInt(availableTickets.size()); // select a random index
                selected.add(availableTickets.remove(index)); // add the selected ticket and remove it from the available list
            }
        }
        return selected; // return the list of selected tickets
    }

    /**
     * records a successful booking by incrementing the corresponding counter
     */
    private void recordSuccess() {
        successfulBookings.incrementAndGet(); // increment the successful bookings counter
    }

    /**
     * records a failed booking by incrementing the corresponding counter
     */
    private void recordFailure() {
        failedBookings.incrementAndGet(); // increment the failed bookings counter
    }

    /**
     * prints the final metrics and results of the simulation, including the number of
     * successful and failed bookings, concurrency conflicts, and schema modification time
     */
    private void printFinalMetrics(int eventId, int initialTicketCount) {
        // get the current list of available ticket serials after the simulation
        List<String> currentTickets = bookingService.getAvailableTicketSerials(eventId);
        // calculate the total number of tickets booked during the simulation
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
