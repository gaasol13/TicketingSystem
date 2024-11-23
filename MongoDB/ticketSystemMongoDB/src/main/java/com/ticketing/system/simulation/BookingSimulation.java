package com.ticketing.system.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.bson.types.ObjectId;

import com.poortoys.examples.dao.BookingDAO;
import com.poortoys.examples.dao.EventDAO;
import com.poortoys.examples.dao.TicketDAO;
import com.poortoys.examples.dao.UserDAO;
import com.poortoys.examples.initializer.DataInitializer;
import com.ticketing.system.entities.User;

import dev.morphia.Datastore;

public class BookingSimulation {
	
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

	             // Get initial ticket count
	             long initialTicketCount = ticketDAO.countAvailableTickets(eventId);

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
	         
	         System.out.println("\nTransaction Metrics:");
	         System.out.println("Total Booking Attempts: " + NUM_USERS);
	         System.out.println("Successful Bookings: " + successfulBookings);
	         System.out.println("Failed Bookings: " + failedBookings);
	         
	         System.out.println("\nInventory Metrics:");
	         System.out.println("Initial Tickets: " + initialTicketCount);
	         System.out.println("Total Booked: " + (initialTicketCount - remainingTickets));
	         System.out.println("Remaining Tickets: " + remainingTickets);
	         
	         System.out.println("===============================\n");
	     }

}