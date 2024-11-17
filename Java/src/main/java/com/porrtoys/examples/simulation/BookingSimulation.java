package com.porrtoys.examples.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BookingSimulation {
    // Use smaller numbers for testing
    private static final int NUM_USERS = 5;  // Reduced from 100
    private static final int MAX_TICKETS_PER_USER = 2;
    private static final int THREAD_POOL_SIZE = 5;  // Reduced from 50

    private final BookingService bookingService;
    private final ExecutorService executorService;
    private final AtomicInteger successfulBookings;
    private final AtomicInteger failedBookings;

    public BookingSimulation(BookingService bookingService) {
        this.bookingService = bookingService;
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        this.successfulBookings = new AtomicInteger(0);
        this.failedBookings = new AtomicInteger(0);
    }

    public void runSimulation(int eventId) {
        System.out.println("Starting BookingSimulation for Event ID: " + eventId);
        
        // First get available tickets
        List<String> availableTickets = bookingService.getAvailableTicketSerials(eventId);
        System.out.println("Found " + availableTickets.size() + " available tickets");

        // Create tasks for each user
        Random random = new Random();
        List<Runnable> tasks = new ArrayList<>();

        for (int userId = 1; userId <= NUM_USERS; userId++) {
            final int finalUserId = userId;
            
            Runnable bookingTask = () -> {
                try {
                    // Each user tries to book 1-2 tickets
                    int numTickets = 1 + random.nextInt(MAX_TICKETS_PER_USER);
                    
                    // Select random tickets
                    List<String> selectedTickets = new ArrayList<>();
                    synchronized(availableTickets) {
                        for (int i = 0; i < numTickets && !availableTickets.isEmpty(); i++) {
                            int index = random.nextInt(availableTickets.size());
                            selectedTickets.add(availableTickets.remove(index));
                        }
                    }
                    
                    if (!selectedTickets.isEmpty()) {
                        // Try to create booking
                        try {
                            bookingService.createBooking(finalUserId, selectedTickets, 
                                "user" + finalUserId + "@example.com");
                            successfulBookings.incrementAndGet();
                            System.out.println("Successfully booked tickets for user " + finalUserId);
                        } catch (Exception e) {
                            System.err.println("Booking failed for user " + finalUserId + 
                                ": " + e.getMessage());
                            failedBookings.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Task failed for user " + finalUserId + ": " + e.getMessage());
                    failedBookings.incrementAndGet();
                }
            };
            
            executorService.execute(bookingTask);
        }

        // Shutdown executor and wait for tasks to complete
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.err.println("Simulation interrupted: " + e.getMessage());
        }

        // Print results
        System.out.println("\n=== Simulation Results ===");
        System.out.println("Successful Bookings: " + successfulBookings.get());
        System.out.println("Failed Bookings: " + failedBookings.get());
        System.out.println("Total Attempts: " + (successfulBookings.get() + failedBookings.get()));
        System.out.println("========================\n");
    }
}