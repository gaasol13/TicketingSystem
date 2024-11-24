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

/**
 * Class responsible for simulating the booking process in a MySQL-based ticketing system.
 * It tests both transaction management and schema modifications to evaluate MySQL's capabilities.
 */
public class BookingSimulation {

    // Configuration Constants
    private static final int NUM_USERS = 100;
    private static final int MAX_TICKETS_PER_USER = 2;
    private static final int THREAD_POOL_SIZE = 10;
    private static final int SIMULATION_TIMEOUT_MINUTES = 1;
    private static final int BATCH_SIZE = 100;

    // Metrics fields
    protected long simulationStartTime;
    protected long simulationEndTime;
    private long schemaModificationTime = 0; // Time taken to modify the schema

    // Dependencies for the simulation
    private final BookingService bookingService;
    private final UserDAO userDAO;
    private final EventDAO eventDAO;
    private final TicketDAO ticketDAO;
    private final ExecutorService executorService;

    // Atomic counters for tracking simulation metrics
    private final AtomicInteger successfulBookings = new AtomicInteger(0);
    private final AtomicInteger failedBookings = new AtomicInteger(0);

    /**
     * Constructor for BookingSimulation.
     *
     * @param bookingService Service handling booking operations
     * @param userDAO        DAO for user-related operations
     */
    public BookingSimulation(BookingService bookingService, UserDAO userDAO,EventDAO eventDAO, TicketDAO ticketDAO) {
        this.bookingService = bookingService;
        this.userDAO = userDAO;
        this.eventDAO = eventDAO;
        this.ticketDAO = ticketDAO;
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
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
            System.out.println("Event ID: " + eventId);
            System.out.println("Initial ticket count: " + initialTicketCount);

            // Start simulation timing
            simulationStartTime = System.nanoTime();

            // Execute the positive scenario to test transaction consistency
            runPositiveScenario(eventId);

            // Execute the negative scenario to test schema modification under concurrency
            runNegativeScenario();

            // End simulation timing
            simulationEndTime = System.nanoTime();

            // Print the final metrics after the simulation
            printFinalMetrics(eventId, initialTicketCount);
        } catch (Exception e) {
            System.err.println("Simulation failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
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

        CountDownLatch completionLatch = new CountDownLatch(NUM_USERS);

        // Get available tickets for the event
        List<String> availableTickets = bookingService.getAvailableTicketSerials(eventId);

        // Get all users from the database
        List<User> users = userDAO.findAll();

        Random random = new Random();

        for (int i = 0; i < NUM_USERS; i++) {
            executorService.submit(() -> {
                try {
                    // Select a random user from the list
                    User user = users.get(random.nextInt(users.size()));

                    // Decide how many tickets the user will try to book (1 to MAX_TICKETS_PER_USER)
                    int ticketsToBook = 1 + random.nextInt(MAX_TICKETS_PER_USER);

                    List<String> selectedTickets;

                    synchronized (availableTickets) {
                        selectedTickets = selectRandomTickets(availableTickets, ticketsToBook);
                    }

                    if (selectedTickets.isEmpty()) {
                        failedBookings.incrementAndGet();
                        return;
                    }

                    try {
                        // Attempt to create a booking for the user
                        Booking booking = bookingService.createBooking(user.getUserId(), selectedTickets, user.getEmail());
                        if (booking != null) {
                            successfulBookings.incrementAndGet();
                        } else {
                            failedBookings.incrementAndGet();
                        }
                    } catch (Exception e) {
                        failedBookings.incrementAndGet();
                    }
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        try {
            // Wait for all tasks to complete or until the timeout expires
            if (!completionLatch.await(SIMULATION_TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
                System.err.println("Simulation timed out before completion.");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Runs the negative scenario of the simulation, which tests the system's ability
     * to handle schema modifications under concurrency.
     */
    private void runNegativeScenario() {
        System.out.println("Running schema modification scenario...");

        try {
            // Example operation: drop user columns
            schemaModificationTime = bookingService.modifySchema("drop_user_columns");
        } catch (Exception e) {
            System.err.println("Failed to modify users table: " + e.getMessage());
        }
    }

    /**
     * Selects a random subset of tickets from the available tickets list.
     *
     * @param availableTickets List of available tickets
     * @param count            Number of tickets to select
     * @return List of selected tickets
     */
    private List<String> selectRandomTickets(List<String> availableTickets, int count) {
        List<String> selected = new ArrayList<>();
        Random random = new Random();

        synchronized (availableTickets) {
            for (int i = 0; i < count && !availableTickets.isEmpty(); i++) {
                int index = random.nextInt(availableTickets.size());
                selected.add(availableTickets.remove(index));
            }
        }
        return selected;
    }

    /**
     * Prints the final metrics and results of the simulation.
     *
     * @param eventId            ID of the event
     * @param initialTicketCount Number of tickets available at the start
     */
    private void printFinalMetrics(int eventId, int initialTicketCount) {
        // Get the current list of available ticket serials after the simulation
    	Event event = eventDAO.findById(eventId);
        List<String> currentTickets = bookingService.getAvailableTicketSerials(eventId);
        // Calculate the total number of tickets booked during the simulation
        int totalTicketsBooked = initialTicketCount - currentTickets.size();

        System.out.println("\n=== Database Simulation Results ===");
        System.out.println("Configuration:");
        System.out.println("Concurrent Users: " + NUM_USERS);
        System.out.println("Max Tickets Per User: " + MAX_TICKETS_PER_USER);
        System.out.println("Thread Pool Size: " + THREAD_POOL_SIZE);

        System.out.println("\nPerformance Metrics:");
        System.out.println("Total Simulation Time: " +
                (simulationEndTime - simulationStartTime) / 1_000_000 + " ms");
        System.out.println("Average Query Time: " +
                bookingService.getAverageQueryTime() + " ms");
        System.out.println("Total Queries Executed: " +
                bookingService.getTotalQueries());
        System.out.println("Schema Modification Time: " + schemaModificationTime + " ms");

        System.out.println("\nTransaction Metrics:");
        System.out.println("Total Booking Attempts: " + NUM_USERS);
        System.out.println("Successful Bookings: " + successfulBookings.get());
        System.out.println("Failed Bookings: " + failedBookings.get());


        System.out.println("\nInventory Metrics:");
		 System.out.println("\nEvent: " + event.getEventName()); 
		
		//System.out.println("Initial ticket count: " + ticketDAO.findAvailableTicketsByEventId(eventId));
        System.out.println("Initial Tickets: " + initialTicketCount);
        System.out.println("Total Booked: " + totalTicketsBooked);
        System.out.println("Remaining Tickets: " + currentTickets.size());

        System.out.println("===============================\n");
    }
}