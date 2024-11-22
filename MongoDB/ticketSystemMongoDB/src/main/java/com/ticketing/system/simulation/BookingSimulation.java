package com.ticketing.system.simulation;

// necessary imports for the simulation functionality
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.poortoys.examples.dao.BookingDAO;
import com.poortoys.examples.dao.EventDAO;
import com.poortoys.examples.dao.TicketDAO;
import com.poortoys.examples.dao.UserDAO;
import com.ticketing.system.entities.Event;
import com.ticketing.system.entities.User;

import dev.morphia.Datastore;

/**
 * class responsible for simulating the booking process in the ticketing system.
 * it tests both positive and negative scenarios to evaluate MongoDB's capabilities
 * in handling dynamic documents and concurrent operations.
 */
public class BookingSimulation {
    // Standardized test parameters
    private static final int NUM_USERS = 5;                      // number of users to simulate
    private static final int MAX_TICKETS_PER_USER = 4;           // Maximum number of tickets a user can book
    private static final int THREAD_POOL_SIZE = 5;               // Number of threads in the thread pool
    private static final String DATABASE_TYPE = "MongoDB";       // type of database used (for informational purposes)

    // Dependencies for the simulation
    private final BookingService bookingService;                  // service handling booking operations
    private final UserDAO userDAO;                                // dao for user-related database operations
    private final EventDAO eventDAO;                              // DAO for event-related database operations
    private final TicketDAO ticketDAO;                            // DAO for ticket-related database operations
    private final Datastore datastore;                            // Morphia Datastore for MongoDB interactions
    private final ExecutorService executorService;                // executorService for managing concurrent tasks

    // Atomic counters for tracking simulation metrics in a thread-safe manner
    private final AtomicInteger totalAttempts = new AtomicInteger(0);          // total number of booking attempts
    private final AtomicInteger successfulBookings = new AtomicInteger(0);     // number of successful bookings
    private final AtomicInteger failedBookings = new AtomicInteger(0);         // Number of failed bookings
    private final AtomicInteger concurrencyConflicts = new AtomicInteger(0);   // Number of concurrency conflicts encountered
    private final AtomicInteger dynamicFieldUpdates = new AtomicInteger(0);    // Number of dynamic field updates performed

    /**
     * constructor for BookingSimulation.
     * initializes the required DAOs, BookingService, and ExecutorService.
     */
    public BookingSimulation(Datastore datastore, BookingDAO bookingDAO,
                             UserDAO userDAO, EventDAO eventDAO, TicketDAO ticketDAO) {
        this.datastore = datastore;
        this.userDAO = userDAO;
        this.eventDAO = eventDAO;
        this.ticketDAO = ticketDAO;
        this.bookingService = new BookingService(bookingDAO, ticketDAO, userDAO, eventDAO, datastore);
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE); // Initialize a fixed thread pool
    }

    /**
     * runs the booking simulation for a specific event.
     * it tests both positive and negative scenarios to evaluate MongoDB's capabilities
     * in handling dynamic documents and concurrent operations.
     */
    public void runSimulation(ObjectId eventId, int numUsers, int maxTicketsPerUser) {
        try {
            // retrieve the event details from the database
            Event event = eventDAO.findById(eventId);
            if (event == null) {
                throw new RuntimeException("Event not found: " + eventId);
            }

            // Print simulation start information
            System.out.println("\n=== Starting MongoDB Simulation ===");
            System.out.println("Event: " + event.getName());
            System.out.println("Initial ticket count: " + ticketDAO.countAvailableTickets(eventId));

            // Run the positive scenario to test dynamic ticket creation
            runDynamicTicketScenario(eventId);

            // Run the negative scenario to test concurrency handling during bookings
            runConcurrentBookingScenario(eventId, numUsers, maxTicketsPerUser);

            // Print the final results of the simulation
            printResults(eventId, event.getName());
        } catch (Exception e) {
            // handle any exceptions that occur during the simulation
            System.err.println("Simulation failed: " + e.getMessage());
        } finally {
            // ensure that resources are properly shut down after the simulation
            shutdown();
        }
    }

    /**
     * runs the positive scenario of the simulation, which tests the creation of tickets
     * with dynamic fields to evaluate MongoDB's schema flexibility.
     */
    private void runDynamicTicketScenario(ObjectId eventId) {
        long startTime = System.currentTimeMillis(); // record the start time of the operation

        try {
            // define dynamic fields to be added to the ticket
            Map<String, Object> dynamicFields = new HashMap<>();
            dynamicFields.put("vipAccess", true); // indicates if VIP access is included
            dynamicFields.put("merchandiseIncluded", Arrays.asList("t-shirt", "poster")); // list of merchandise items included
            dynamicFields.put("specialOffer", new Document()
                .append("discount", 20)               // discount percentage
                .append("validUntil", new Date()));    // expiration date of the offer

            // attempt to create a dynamic ticket using the BookingService
            boolean success = bookingService.createDynamicTicket(eventId, dynamicFields);

            // Calculate the time taken to update dynamic fields
            System.out.println("\nDynamic Ticket Creation Results:");
            System.out.println("- Status: " + (success ? "Successful" : "Failed"));
            System.out.println("- Processing time: " + (System.currentTimeMillis() - startTime) + "ms");
            System.out.println("- Fields added: " + dynamicFields.size());
        } catch (Exception e) {
            // handle any exceptions that occur during dynamic ticket creation
            System.err.println("Dynamic ticket scenario failed: " + e.getMessage());
            System.out.println("- Processing time: " + (System.currentTimeMillis() - startTime) + "ms");
        }
    }

    /**
     * runs the negative scenario of the simulation, which tests concurrent bookings
     * to evaluate MongoDB's handling of concurrency and atomic operations.
     */
    private void runConcurrentBookingScenario(ObjectId eventId, int numUsers, int maxTicketsPerUser) {
        // retrieve all users from the database
        List<User> users = userDAO.findAll();
        if (users.size() < numUsers) {
            System.out.println("Warning: Only " + users.size() + " users available for simulation");
            numUsers = users.size(); // adjust the number of users if not enough are available
        }

        List<Callable<Boolean>> tasks = new ArrayList<>(); // list to hold booking tasks
        Random random = new Random(); // random number generator for selecting users and tickets

        for (int i = 0; i < numUsers; i++) {
            User user = users.get(random.nextInt(users.size())); // randomly select a user
            int ticketsToBook = random.nextInt(maxTicketsPerUser) + 1; // randomly decide how many tickets to book (1 to max)

            // create a booking task for the selected user and number of tickets
            tasks.add(() -> {
                totalAttempts.incrementAndGet(); // increment the total booking attempts
                long startTime = System.currentTimeMillis(); // record the start time of the booking attempt

                // attempt to book tickets using the BookingService
                boolean success = bookingService.bookTickets(user.getId(), eventId, ticketsToBook);

                // update the booking counters based on the result
                if (success) {
                    successfulBookings.incrementAndGet(); // increment successful bookings if the booking was successful
                } else {
                    failedBookings.incrementAndGet(); // increment failed bookings if the booking failed
                }

                return success; // indicate whether the booking was successful
            });
        }

        try {
            // submit all booking tasks to the executor service and wait for their completion
            executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            // Handle interruptions during task execution
            Thread.currentThread().interrupt(); // restore the interrupted status
            System.err.println("Booking simulation interrupted: " + e.getMessage());
        }
    }

    /**
     * prints the results of the simulation, including metrics from both positive and
     * negative scenarios.
     */
    private void printResults(ObjectId eventId, String eventName) {
        System.out.println("\n=== MongoDB Simulation Results ===");
        System.out.println("Event: " + eventName);

        // Print results from the positive scenario
        System.out.println("\nPOSITIVE SCENARIO Results:");
        System.out.println("- Dynamic fields processed: " + bookingService.getDynamicFieldUpdates());

        // Print results from the negative scenario
        System.out.println("\nNEGATIVE SCENARIO Results:");
        System.out.println("- Total booking attempts: " + totalAttempts.get());
        System.out.println("- Successful bookings: " + successfulBookings.get());
        System.out.println("- Failed bookings: " + failedBookings.get());
        System.out.println("- Concurrency conflicts: " + bookingService.getConcurrencyConflicts());
        System.out.println("- Remaining tickets: " + ticketDAO.countAvailableTickets(eventId));

        // Calculate and print the concurrency conflict rate
        double conflictRate = totalAttempts.get() > 0 
            ? ((double) bookingService.getConcurrencyConflicts() / totalAttempts.get()) * 100 
            : 0.0;
        System.out.println("=======================\n");
    }

    /**
     * shuts down the executor service gracefully, ensuring that all tasks are completed
     * or forcefully terminated if they exceed the shutdown timeout.
     */
    private void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown(); // initiate shutdown of the executor service
            try {
                // await termination for a specified timeout
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow(); // force shutdown if not terminated within timeout
                    System.out.println("Forcing shutdown of executor service...");
                }
                System.out.println("Simulation executor service shutdown complete.");
            } catch (InterruptedException e) {
                // handle interruptions during shutdown
                executorService.shutdownNow(); // force shutdown
                Thread.currentThread().interrupt(); // restore the interrupted status
                System.err.println("Simulation shutdown interrupted: " + e.getMessage());
            }
        }
    }
}
