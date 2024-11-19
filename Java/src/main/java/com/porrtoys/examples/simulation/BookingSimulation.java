package com.porrtoys.examples.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BookingSimulation {
    // Use smaller numbers for testing
    private static final int NUM_USERS = 1000;  // Reduced from 100
    private static final int MAX_TICKETS_PER_USER = 2;
    private static final int THREAD_POOL_SIZE = 10;  // Reduced from 50
    private static final int SIMULATION_TIMEOUT_MINUTES = 1;
    private static final String DATABASE_TYPE = " MySQL";

    private final BookingService bookingService;
    private final ExecutorService executorService;
    private final AtomicInteger successfulBookings;
    private final AtomicInteger failedBookings;

    private int initialTicketCount;
    private int eventId; 
    
    public BookingSimulation(BookingService bookingService) {
        this.bookingService = bookingService;
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        this.successfulBookings = new AtomicInteger(0);
        this.failedBookings = new AtomicInteger(0);
    }

    public void runSimulation(int eventId) {
        this.eventId = eventId;
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<Callable<Boolean>> tasks = new ArrayList<>();

        // Get initial ticket count like MongoDB
        List<String> availableTickets = bookingService.getAvailableTicketSerials(eventId);
        initialTicketCount = availableTickets.size();
        System.out.println("Found " + initialTicketCount + " available tickets");

        Random random = new Random();
        
        // Create tasks similar to MongoDB
        for(int userId = 1; userId <= NUM_USERS; userId++) {
            final int finalUserId = userId;
            final int ticketsToBook = 1 + random.nextInt(MAX_TICKETS_PER_USER);
            
            tasks.add(() -> {
                try {
                    List<String> selectedTickets = selectTickets(availableTickets, ticketsToBook);
                    if(!selectedTickets.isEmpty()) {
                        bookingService.createBooking(finalUserId, selectedTickets, 
                            "user" + finalUserId + "@example.com");
                        successfulBookings.incrementAndGet();
                        return true;
                    }
                    failedBookings.incrementAndGet();
                    return false;
                } catch (Exception e) {
                    failedBookings.incrementAndGet();
                    System.out.println("Booking failed for user " + finalUserId + ": " + e.getMessage());
                    return false;
                }
            });
        }

        // Execute tasks like MongoDB
        try {
            List<Future<Boolean>> results = executorService.invokeAll(tasks);
            executorService.shutdown();
            executorService.awaitTermination(SIMULATION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            
            printFinalMetrics();
        } catch (InterruptedException e) {
            System.err.println("Simulation interrupted: " + e.getMessage());
        }
    }

    private List<String> selectTickets(List<String> availableTickets, int count) {
        List<String> selected = new ArrayList<>();
        synchronized(availableTickets) {
            for(int i = 0; i < count && !availableTickets.isEmpty(); i++) {
                int randomIndex = new Random().nextInt(availableTickets.size());
                selected.add(availableTickets.remove(randomIndex));
            }
        }
        return selected;
    }
       
    private void printFinalMetrics() {
    	
    	 List<String> currentTickets = bookingService.getAvailableTicketSerials(eventId);
        System.out.println("\n=== Simulation Results ===");
        System.out.println("Database Type: " + DATABASE_TYPE);
        System.out.println("Concurrent Users: " + NUM_USERS);
        System.out.println("Total booking attempts: " + NUM_USERS);
        System.out.println("Total tickets available before booking: " + initialTicketCount);
        
       System.out.println((currentTickets));
        System.out.println("Total tickets booked: " + (initialTicketCount - currentTickets.size()));
        System.out.println("Successful bookings: " + successfulBookings.get());
        System.out.println("Failed bookings: " + failedBookings.get());
        System.out.println("Tickets remaining: " + currentTickets.size());
        System.out.println("=======================\n");
    }


}