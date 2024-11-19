package com.ticketing.system.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.bson.types.ObjectId;

import com.poortoys.examples.dao.BookingDAO;
import com.poortoys.examples.dao.EventDAO;
import com.poortoys.examples.dao.TicketDAO;
import com.poortoys.examples.dao.UserDAO;
import com.poortoys.examples.initializer.DataInitializer;
import com.ticketing.system.entities.User;

import dev.morphia.Datastore;

public class BookingSimulation {
	
	// Standardized test parameters
    private static final int NUM_USERS = 1000;
    private static final int MAX_TICKETS_PER_USER = 2;
    private static final int THREAD_POOL_SIZE = 10;
    private static final int SIMULATION_TIMEOUT_MINUTES = 1;
    private static final String DATABASE_TYPE = "MongoDB";
    
    // Keep track of results the same way
    private final AtomicInteger successfulBookings = new AtomicInteger(0);
    private final AtomicInteger failedBookings = new AtomicInteger(0);

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
	     public void runSimulation(ObjectId eventId, int numUsers, int maxTicketsPerUser) {
	         ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE); // Adjust thread pool size as needed
	         List<Callable<Boolean>> tasks = new ArrayList<>();

	         // Retrieve all users to simulate booking attempts
	         List<User> users = userDAO.findAll();

	         // If not enough users, create dummy users
	         if (users.size() < numUsers) {
	             System.out.println("Not enough users in the system. Please add more users for the simulation.");
	             executor.shutdown();
	             return;
	         }

	         Random random = new Random();

	         for (int i = 0; i < numUsers; i++) {
	             final User user = users.get(random.nextInt(users.size()));
	             final int ticketsToBook = random.nextInt(maxTicketsPerUser) + 1; // 1 to maxTicketsPerUser

	             Callable<Boolean> task = () -> {
	                 return bookingService.bookTickets(user.getId(), eventId, ticketsToBook);
	             };

	             tasks.add(task);
	         }

	         try {
	             List<Future<Boolean>> results = executor.invokeAll(tasks);

	             // Wait for all tasks to complete
	             executor.shutdown();
	             executor.awaitTermination(1, TimeUnit.MILLISECONDS);        
	             
	             

	             // Verify no overselling	
	             long totalBookedTickets = bookingDAO.findAll().stream()
	                     .filter(booking -> booking.getEventId().equals(eventId))
	                     .mapToInt(booking -> booking.getTickets().size())
	                     .sum();

	             long totalTickets = ticketDAO.countAvailableTickets(eventId) + totalBookedTickets;

	             
	             System.out.println("\n=== Simulation Results ===");
		         System.out.println("Database Type: " + DATABASE_TYPE);
		         System.out.println("Concurrent Users: " + NUM_USERS);
		         System.out.println("Total booking attempts: " + numUsers);
	             System.out.println("Total tickets available before booking: " + totalTickets);
	             System.out.println("Total tickets booked: " + totalBookedTickets);
	             System.out.println("Successful bookings: " + bookingService.getSuccessfulBookings());
	             System.out.println("Failed bookings: " + bookingService.getFailedBookings());
	             System.out.println("Tickets remaining: " + ticketDAO.countAvailableTickets(eventId));

	             System.out.println("=======================\n");
	             
	         } catch (InterruptedException e) {
	             System.err.println("Simulation interrupted: " + e.getMessage());
	             e.printStackTrace();
	         }
	     }

	     

}
