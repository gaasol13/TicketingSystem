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
/**
 * Simulates concurrent booking operations in a MySQL-based ticketing system.
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
    private final ExecutorService executorService;

    // Metrics
    private long simulationStartTime;
    private long simulationEndTime;
    private final AtomicInteger successfulBookings = new AtomicInteger(0);
    private final AtomicInteger failedBookings = new AtomicInteger(0);
    private int initialTicketCount;
    private Event event;

    public BookingSimulation(BookingService bookingService, UserDAO userDAO, 
                           EventDAO eventDAO, TicketDAO ticketDAO) {
        this.bookingService = bookingService;
        this.userDAO = userDAO;
        this.eventDAO = eventDAO;
        this.ticketDAO = ticketDAO;
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    /**
     * Runs the concurrent booking simulation for a specific event.
     */
    public void runSimulation(int eventId) {
        try {
            initializeSimulation(eventId);
            executeBookingTasks(eventId);
            waitForCompletion();
            printSimulationResults(eventId);
            
        } catch (Exception e) {
            handleSimulationError(e);
        } finally {
            cleanupResources();
        }
    }

    private void initializeSimulation(int eventId) {
        System.out.println("\n=== Starting MySQL Booking Simulation ===");
        event = eventDAO.findById(eventId);
        if (event == null) {
            throw new RuntimeException("Event not found: " + eventId);
        }

        initialTicketCount = bookingService.getAvailableTicketSerials(eventId).size();
        System.out.println("Event: " + event.getEventName());
        System.out.println("Initial ticket count: " + initialTicketCount);
        simulationStartTime = System.nanoTime();
    }

    private void executeBookingTasks(int eventId) {
        CountDownLatch completionLatch = new CountDownLatch(NUM_USERS);
        List<String> availableTickets = bookingService.getAvailableTicketSerials(eventId);
        List<User> users = userDAO.findAll();
        Random random = new Random();

        for (int i = 0; i < NUM_USERS; i++) {
            executorService.submit(() -> {
                try {
                    executeBookingAttempt(users, availableTickets, random);
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

    private void executeBookingAttempt(List<User> users, List<String> availableTickets, 
                                     Random random) {
        User user = users.get(random.nextInt(users.size()));
        int ticketsToBook = 1 + random.nextInt(MAX_TICKETS_PER_USER);
        List<String> selectedTickets;

        synchronized (availableTickets) {
            selectedTickets = selectRandomTickets(availableTickets, ticketsToBook);
        }

        if (!selectedTickets.isEmpty()) {
            try {
                Booking booking = bookingService.createBooking(
                    user.getUserId(), 
                    selectedTickets, 
                    user.getEmail()
                );
                if (booking != null) {
                    successfulBookings.incrementAndGet();
                } else {
                    failedBookings.incrementAndGet();
                }
            } catch (Exception e) {
                failedBookings.incrementAndGet();
                System.err.println("Booking failed for user " + user.getUserId() + 
                                 ": " + e.getMessage());
            }
        } else {
            failedBookings.incrementAndGet();
        }
    }

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

    private void waitForCompletion() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(SIMULATION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        simulationEndTime = System.nanoTime();
    }

    private void printSimulationResults(int eventId) {
        List<String> currentTickets = bookingService.getAvailableTicketSerials(eventId);
        int totalBooked = initialTicketCount - currentTickets.size();

        System.out.println("\n=== Simulation Results ===");
        
        // Configuration metrics
        System.out.println("Configuration:");
        System.out.printf("Concurrent Users: %d%n", NUM_USERS);
        System.out.printf("Max Tickets Per User: %d%n", MAX_TICKETS_PER_USER);
        System.out.printf("Thread Pool Size: %d%n", THREAD_POOL_SIZE);

     // Event details
        System.out.println("\nEvent Details:");
        System.out.printf("Event: %s%n", event.getEventName());
        System.out.printf("Venue: %s%n", event.getVenue().getVenueName());
        
        // Performance metrics
        System.out.println("\nPerformance Metrics:");
        long duration = (simulationEndTime - simulationStartTime) / 1_000_000;
        System.out.printf("Total Simulation Time: %d ms%n", duration);
        System.out.printf("Average Query Time: %.2f ms%n", 
            bookingService.getAverageQueryTime());
        System.out.printf("Total Queries: %d%n", 
            bookingService.getTotalQueries());

        // Booking metrics
        System.out.println("\nBooking Results:");
        System.out.printf("Successful Bookings: %d%n", successfulBookings.get());
        System.out.printf("Failed Bookings: %d%n", failedBookings.get());
        System.out.printf("Success Rate: %.2f%%%n", 
            (double)successfulBookings.get() / NUM_USERS * 100);

        // Inventory metrics
        System.out.println("\nInventory Status:");
        System.out.printf("Initial Available Tickets: %d%n", initialTicketCount);
        System.out.printf("Total Tickets Booked: %d%n", totalBooked);
        System.out.printf("Remaining Available: %d%n", currentTickets.size());

     // Verify consistency
		/*
		 * boolean isConsistent = (initialTicketCount - currentTickets.size()) ==
		 * currentTickets; System.out.printf("\nData Consistency Check: %s%n",
		 * isConsistent ? "PASSED" : "FAILED");
		 */

        System.out.println("===============================\n");
    }
    private void validateTicketQuantity(int quantity) {
        if (quantity <= 0 || quantity > MAX_TICKETS_PER_USER) {
            throw new IllegalArgumentException("Invalid ticket quantity: " + quantity);
        }
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