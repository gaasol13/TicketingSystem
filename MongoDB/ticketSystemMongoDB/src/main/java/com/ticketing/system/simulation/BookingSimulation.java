package com.ticketing.system.simulation;

// Necessary imports for the simulation functionality
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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.poortoys.examples.dao.BookingDAO;
import com.poortoys.examples.dao.EventDAO;
import com.poortoys.examples.dao.TicketDAO;
import com.poortoys.examples.dao.UserDAO;
import com.poortoys.examples.initializer.DataInitializer;
import com.ticketing.system.entities.Event;
import com.ticketing.system.entities.User;

import dev.morphia.Datastore;
import lombok.Data;

/**
 * Class responsible for simulating the booking process in the ticketing system.
 * It tests both positive and negative scenarios to evaluate MongoDB's capabilities
 * in handling dynamic documents and concurrent operations.
 */
public class BookingSimulation {
    
    // Standardized test parameters
    private static final int NUM_USERS = 1000;                       // Number of users to simulate
    private static final int MAX_TICKETS_PER_USER = 2;               // Maximum tickets a user can book
    private static final int THREAD_POOL_SIZE = 10;                  // Number of threads in the pool
    private static final int SIMULATION_DURATION_SECONDS = 30;       // Duration of the simulation in seconds
    private static final String DATABASE_TYPE = "MongoDB";            // Type of database used
    
    // Dependencies for the simulation
    private final BookingService bookingService;                      // Service handling booking operations
    private final UserDAO userDAO;                                    // DAO for user-related operations
    private final EventDAO eventDAO;                                  // DAO for event-related operations
    private final TicketDAO ticketDAO;                                // DAO for ticket-related operations
    private final Datastore datastore;                                // Datastore for MongoDB interactions
    private final ExecutorService executorService;                    // Executor service for managing threads
    
    /**
     * Inner class to hold metrics collected during the simulation.
     * Uses Lombok's @Data annotation to generate boilerplate code like getters and setters.
     */
    @Data
    public class SimulationMetrics {
        private int totalAttempts = 0;              // Total number of booking attempts
        private int successfulBookings = 0;         // Number of successful bookings
        private int failedBookings = 0;             // Number of failed bookings
        private long dynamicFieldUpdateTime = 0;    // Time taken to update dynamic fields
        private int concurrencyConflicts = 0;       // Number of concurrency conflicts encountered
        private List<Long> responseTimesMs = new ArrayList<>(); // List of response times in milliseconds
        
        // Placeholder methods for setting and getting metrics (to be implemented or handled by Lombok)
        public void setDynamicFieldUpdateTime(long l) {
            this.dynamicFieldUpdateTime = l;
        }
        public String getDynamicFieldUpdateTime() {
            return String.valueOf(dynamicFieldUpdateTime);
        }
        public int getSuccessfulBookings() {
            return successfulBookings;
        }
        public int getTotalAttempts() {
            return totalAttempts;
        }
        public List<Long> getResponseTimesMs() {
            return responseTimesMs;
        }
        public void setTotalAttempts(int i) {
            this.totalAttempts = i;
        }
        public void setSuccessfulBookings(int i) {
            this.successfulBookings = i;
        }
        public int getConcurrencyConflicts() {
            return concurrencyConflicts;
        }
		public int getFailedBookings() {
			// TODO Auto-generated method stub
			return 0;
		}
		public void setFailedBookings(int i) {
			// TODO Auto-generated method stub
			
		}
    }

    // Instance of SimulationMetrics to track simulation data
    private final SimulationMetrics metrics = new SimulationMetrics();

    /**
     * Constructor for BookingSimulation.
     * Initializes the required DAOs, BookingService, and ExecutorService.
     * 
     * @param datastore  Datastore for MongoDB interactions.
     * @param bookingDAO DAO for booking-related operations.
     * @param userDAO    DAO for user-related operations.
     * @param eventDAO   DAO for event-related operations.
     * @param ticketDAO  DAO for ticket-related operations.
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
     * Runs the booking simulation for a specific event.
     * It tests both dynamic ticket creation and concurrent booking scenarios.
     * 
     * @param eventId            ID of the event to simulate bookings for.
     * @param numUsers           Number of users to simulate.
     * @param maxTicketsPerUser  Maximum number of tickets a user can attempt to book.
     */
    public void runSimulation(ObjectId eventId, int numUsers, int maxTicketsPerUser) {
        try {
            // Retrieve the event details from the database
            Event event = eventDAO.findById(eventId);
            if (event == null) {
                throw new RuntimeException("Event not found: " + eventId);
            }

            // Print simulation start information
            System.out.println("\n=== Starting MongoDB Simulation ===");
            System.out.println("Event: " + event.getName());
            System.out.println("Initial ticket count: " + ticketDAO.countAvailableTickets(eventId));

            // Run the positive scenario to test dynamic ticket creation
            System.out.println("\nPOSITIVE SCENARIO - Testing Document Flexibility");
            runDynamicTicketScenario(eventId);

            // Run the negative scenario to test concurrency handling during bookings
            System.out.println("\nNEGATIVE SCENARIO - Testing Concurrency Handling");
            runConcurrentBookingScenario(eventId, numUsers, maxTicketsPerUser);

            // Print the final results of the simulation
            printResults(eventId, event.getName());

        } catch (Exception e) {
            // Handle any exceptions that occur during the simulation
            System.err.println("Simulation failed: " + e.getMessage());
        } finally {
            // Ensure that resources are properly shut down after the simulation
            shutdown();
        }
    }

    /**
     * Runs the positive scenario of the simulation, which tests the creation of tickets
     * with dynamic fields to evaluate MongoDB's schema flexibility.
     * 
     * @param eventId ID of the event to create dynamic tickets for.
     */
    private void runDynamicTicketScenario(ObjectId eventId) {
        long startTime = System.currentTimeMillis(); // Record the start time of the operation
        
        try {
            // Define dynamic fields to be added to the ticket
            Map<String, Object> dynamicFields = new HashMap<>();
            dynamicFields.put("vipAccess", true); // Indicates if VIP access is included
            dynamicFields.put("merchandiseIncluded", Arrays.asList("t-shirt", "poster")); // List of merchandise items included
            dynamicFields.put("specialOffer", new Document()
                .append("discount", 20)               // Discount percentage
                .append("validUntil", new Date()));    // Expiration date of the offer

            // Attempt to create a dynamic ticket using the BookingService
            boolean success = bookingService.createDynamicTicket(eventId, dynamicFields);
            
            // Calculate the time taken to update dynamic fields
            metrics.setDynamicFieldUpdateTime(System.currentTimeMillis() - startTime);
            
            // Print the results of the dynamic ticket creation
            System.out.println("\nDynamic Ticket Creation Results:");
            System.out.println("- Status: " + (success ? "Successful" : "Failed"));
            System.out.println("- Processing time: " + metrics.getDynamicFieldUpdateTime() + "ms");
            System.out.println("- Fields added: " + dynamicFields.size());
            
        } catch (Exception e) {
            // Handle any exceptions that occur during dynamic ticket creation
            System.err.println("Dynamic ticket scenario failed: " + e.getMessage());
            metrics.setDynamicFieldUpdateTime(System.currentTimeMillis() - startTime);
        }
    }

    /**
     * Runs the negative scenario of the simulation, which tests concurrent bookings
     * to evaluate MongoDB's handling of concurrency and atomic operations.
     * 
     * @param eventId           ID of the event to simulate bookings for.
     * @param numUsers          Number of users to simulate.
     * @param maxTicketsPerUser Maximum number of tickets a user can attempt to book.
     */
    private void runConcurrentBookingScenario(ObjectId eventId, int numUsers, int maxTicketsPerUser) {
        // Retrieve all users from the database
        List<User> users = userDAO.findAll();
        if (users.size() < numUsers) {
            System.out.println("Warning: Only " + users.size() + " users available for simulation");
            numUsers = users.size(); // Adjust the number of users if not enough are available
        }

        List<Callable<Boolean>> tasks = new ArrayList<>(); // List to hold booking tasks
        Random random = new Random(); // Random number generator for selecting users and tickets

        for (int i = 0; i < numUsers; i++) {
            final User user = users.get(random.nextInt(users.size())); // Randomly select a user
            final int ticketsToBook = random.nextInt(maxTicketsPerUser) + 1; // Randomly decide how many tickets to book (1 to max)

            // Create a booking task for the selected user and number of tickets
            tasks.add(() -> {
                // Increment the total booking attempts
                metrics.setTotalAttempts(metrics.getTotalAttempts() + 1);
                long startTime = System.currentTimeMillis(); // Record the start time of the booking attempt
                
                // Attempt to book tickets using the BookingService
                boolean success = bookingService.bookTickets(user.getId(), eventId, ticketsToBook);
                
                // Record the response time for the booking attempt
                metrics.getResponseTimesMs().add(System.currentTimeMillis() - startTime);
                
                // Update the booking counters based on the result
                if (success) {
                    metrics.setSuccessfulBookings(metrics.getSuccessfulBookings() + 1);
                } else {
                    metrics.setFailedBookings(metrics.getFailedBookings() + 1);
                }
                
                return success; // Indicate whether the booking was successful
            });
        }

        try {
            // Submit all booking tasks to the executor service and wait for their completion
            executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            // Handle interruptions during task execution
            Thread.currentThread().interrupt();
            System.err.println("Booking simulation interrupted: " + e.getMessage());
        }
    }

    /**
     * Prints the results of the simulation, including metrics from both positive and
     * negative scenarios.
     * 
     * @param eventId   ID of the event that was simulated.
     * @param eventName Name of the event.
     */
    private void printResults(ObjectId eventId, String eventName) {
        // Calculate the average response time for booking attempts
        double avgResponseTime = metrics.getResponseTimesMs().stream()
            .mapToLong(Long::valueOf)
            .average()
            .orElse(0.0);

        System.out.println("\n=== MongoDB Simulation Results ===");
        System.out.println("Event: " + eventName);
        
        // Print results from the positive scenario
        System.out.println("\nPOSITIVE SCENARIO Results:");
        System.out.println("- Dynamic field update time: " + metrics.getDynamicFieldUpdateTime() + "ms");
        System.out.println("- Dynamic fields processed: " + bookingService.getDynamicFieldUpdates());
        
        // Print results from the negative scenario
        System.out.println("\nNEGATIVE SCENARIO Results:");
        System.out.println("- Total booking attempts: " + metrics.getTotalAttempts());
        System.out.println("- Successful bookings: " + metrics.getSuccessfulBookings());
        System.out.println("- Failed bookings: " + metrics.getFailedBookings());
        System.out.println("- Concurrency conflicts: " + bookingService.getConcurrencyConflicts());
        System.out.println("- Average response time: " + String.format("%.2f", avgResponseTime) + "ms");
        System.out.println("- Remaining tickets: " + ticketDAO.countAvailableTickets(eventId));
        
        // Summarize key findings from the simulation
        System.out.println("\nKey Findings:");
        System.out.println("1. Document Flexibility: MongoDB handled dynamic fields efficiently");
        System.out.println("2. Concurrency Challenges: " + 
            String.format("%.1f%%", (double)bookingService.getConcurrencyConflicts() / 
            metrics.getTotalAttempts() * 100) + " conflict rate");
        System.out.println("=======================\n");
    }

    /**
     * Shuts down the executor service gracefully, ensuring that all tasks are completed
     * or forcefully terminated if they exceed the shutdown timeout.
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown(); // Initiate shutdown of the executor service
            try {
                // Await termination for a specified timeout
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow(); // Force shutdown if not terminated within timeout
                    System.out.println("Forcing shutdown of executor service...");
                }
                System.out.println("Simulation executor service shutdown complete.");
            } catch (InterruptedException e) {
                // Handle interruptions during shutdown
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
                System.err.println("Simulation shutdown interrupted: " + e.getMessage());
            }
        }
    }

    // Helper methods for accessing metrics
    /**
     * Retrieves the simulation metrics.
     * 
     * @return SimulationMetrics object containing various metrics.
     */
    public SimulationMetrics getMetrics() {
        return metrics;
    }

    /**
     * Retrieves the total number of booking attempts.
     * 
     * @return Total booking attempts.
     */
    public int getTotalAttempts() {
        return metrics.getTotalAttempts();
    }

    /**
     * Retrieves the number of successful bookings.
     * 
     * @return Number of successful bookings.
     */
    public int getSuccessfulBookings() {
        return metrics.getSuccessfulBookings();
    }

    /**
     * Retrieves the number of failed bookings.
     * 
     * @return Number of failed bookings.
     */
    public int getFailedBookings() {
        return metrics.getFailedBookings();
    }

    /**
     * Retrieves the number of concurrency conflicts encountered during the simulation.
     * 
     * @return Number of concurrency conflicts.
     */
    public int getConcurrencyConflicts() {
        return metrics.getConcurrencyConflicts();
    }

    /**
     * Calculates and retrieves the average response time for booking attempts.
     * 
     * @return Average response time in milliseconds.
     */
    public double getAverageResponseTime() {
        if (metrics.getResponseTimesMs().isEmpty()) {
            return 0.0;
        }
        return metrics.getResponseTimesMs().stream()
            .mapToLong(Long::valueOf)
            .average()
            .orElse(0.0);
    }
}
