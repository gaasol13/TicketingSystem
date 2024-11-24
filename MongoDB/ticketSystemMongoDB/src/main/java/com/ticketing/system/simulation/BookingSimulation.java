package com.ticketing.system.simulation;

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

public class BookingSimulation {
	
	  // Standardized configuration
    protected static final int NUM_USERS = 1000;               
    protected static final int MAX_TICKETS_PER_USER = 1;      
    protected static final int THREAD_POOL_SIZE = 10;         
    protected static final int SIMULATION_TIMEOUT_MINUTES = 1; 
    protected static final int BATCH_SIZE = 100;
    
    // Standardized metrics across both implementations
    protected long simulationStartTime;
    protected long simulationEndTime;
    protected long totalQueryTime = 0;
    protected int totalQueries = 0;
    private long initialTicketCount;
    private Event event;

	     private final BookingService bookingService;
	     private final UserDAO userDAO;
	     private final EventDAO eventDAO;
	     private final TicketDAO ticketDAO;
	     private final BookingDAO bookingDAO;
	     private final Datastore datastore;

	     public BookingSimulation(Datastore datastore, BookingDAO bookingDAO, UserDAO userDAO, EventDAO eventDAO, TicketDAO ticketDAO) {
	         this.datastore = datastore;
	         this.bookingDAO = bookingDAO;
	         this.userDAO = userDAO;
	         this.eventDAO = eventDAO;
	         this.ticketDAO = ticketDAO;
	         this.bookingService = new BookingService(bookingDAO, ticketDAO, userDAO, eventDAO, datastore);
	     }

	     /**
	      * Runs the booking simulation.
	      */
	     public void runSimulation(ObjectId eventId) {
	    	 
	         // Get event details first
	         this.event = eventDAO.findById(eventId);
	         if (event == null) {
	             System.out.println("Event not found: " + eventId);
	             return;
	         }

	         // Get initial ticket counts
	         this.initialTicketCount = ticketDAO.countAvailableTickets(eventId);
	         
	         // Print initial state
	         System.out.println("\nStarting simulation for event: " + event.getName());
	         System.out.println("Initial available tickets: " + initialTicketCount);

	         ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
	         List<Callable<Boolean>> tasks = new ArrayList<>();

	         // Retrieve all users to simulate booking attempts
	         List<User> users = userDAO.findAll();

	         // If not enough users, notify and exit
	         if (users.size() < NUM_USERS) {
	             System.out.println("Not enough users in the system. Please add more users for the simulation.");
	             executor.shutdown();
	             return;
	         }

	         Random random = new Random();

	         // Start simulation timing
	         simulationStartTime = System.nanoTime();

	         for (int i = 0; i < NUM_USERS; i++) {
	             final User user = users.get(random.nextInt(users.size()));
	             final int ticketsToBook = random.nextInt(MAX_TICKETS_PER_USER) + 1; // 1 to MAX_TICKETS_PER_USER

	             Callable<Boolean> task = () -> bookingService.bookTickets(user.getId(), eventId, ticketsToBook);

	             tasks.add(task);
	         }

	         try {
	             List<Future<Boolean>> results = executor.invokeAll(tasks);

	             // Wait for all tasks to complete or timeout
	             executor.shutdown();
	             if (!executor.awaitTermination(SIMULATION_TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
	                 executor.shutdownNow();
	             }

	             // End simulation timing
	             simulationEndTime = System.nanoTime();

	             // Collect metrics
	             int successfulBookings = bookingService.getSuccessfulBookings();
	             int failedBookings = bookingService.getFailedBookings();

	          // retrieve the event details from the database
	             Event event = eventDAO.findById(eventId);
	             if (event == null) {
	                 throw new RuntimeException("Event not found: " + eventId);
	             }
	          // Print simulation start information
	             
	             // Get initial ticket count
	             //long initialTicketCount = ticketDAO.countAvailableTickets(eventId);

	             // Get remaining tickets
	             long remainingTickets = ticketDAO.countAvailableTickets(eventId);

	             // Print final metrics
	             printFinalMetrics(eventId, initialTicketCount, successfulBookings, failedBookings, remainingTickets);

	         } catch (InterruptedException e) {
	             System.err.println("Simulation interrupted: " + e.getMessage());
	             Thread.currentThread().interrupt();
	         }
	     }
	     


	     /**
	      * Prints the final metrics after the simulation.
	      */
	     private void printFinalMetrics(ObjectId eventId, long initialTicketCount, int successfulBookings, int failedBookings, long remainingTickets) {
	         System.out.println("\n=== Database Simulation Results ===");
	         
	         // Configuration
	         System.out.println("Configuration:");
	         System.out.printf("Concurrent Users: %d%n", NUM_USERS);
	         System.out.printf("Max Tickets Per User: %d%n", MAX_TICKETS_PER_USER);
	         System.out.printf("Thread Pool Size: %d%n", THREAD_POOL_SIZE);

	         // Event Details
	         System.out.println("\nEvent Details:");
	         System.out.printf("Event: %s%n", event.getName());
	         System.out.printf("Venue: %s%n", event.getVenue().getVenueName());
	         // Clear Ticket Counts
	         System.out.println("\nTicket Summary:");
	         System.out.printf("Initial Available Tickets: %d%n", initialTicketCount);
	        // System.out.printf("Tickets Booked: %d%n", successfulBookings * 2); // Multiply by 2 since each booking can have up to 2 tickets
	         System.out.printf("Remaining Available Tickets: %d%n", remainingTickets);
	         
	         // Booking Results
	         System.out.println("\nBooking Results:");
	         System.out.printf("Successful Bookings: %d%n", successfulBookings);
	         System.out.printf("Failed Bookings: %d%n", failedBookings);
	         System.out.printf("Total Booking Attempts: %d%n", successfulBookings + failedBookings);
	         
	         // Ticket Metrics
	         System.out.println("\nInventory Metrics:");
	         System.out.printf("Initial Available Tickets: %d%n", initialTicketCount);
	         long currentAvailable = ticketDAO.countAvailableTickets(eventId);
	         System.out.printf("Total Tickets Booked: %d%n", initialTicketCount - currentAvailable);
	         System.out.printf("Remaining Available: %d%n", currentAvailable);

	         // Performance Metrics
	         System.out.println("\nPerformance Metrics:");
	         long duration = (simulationEndTime - simulationStartTime) / 1_000_000;
	         System.out.printf("Total Simulation Time: %d ms%n", duration);
	         System.out.printf("Average Query Time: %.2f ms%n", 
	             bookingService.getAverageQueryTime());
	         System.out.printf("Total Queries Executed: %d%n", 
	             bookingService.getTotalQueries());

	         // Transaction Results
	         System.out.println("\nTransaction Metrics:");
	         System.out.printf("Total Attempts: %d%n", NUM_USERS);
	         int successful = bookingService.getSuccessfulBookings();
	         int failed = bookingService.getFailedBookings();
	         System.out.printf("Successful: %d (%.1f%%)%n", 
	             successful, (successful * 100.0 / NUM_USERS));
	         System.out.printf("Failed: %d%n", failed);
	         
	         System.out.println("===============================\n");
	     }

}