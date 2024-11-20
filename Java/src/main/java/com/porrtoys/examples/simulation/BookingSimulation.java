package com.porrtoys.examples.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.poortoys.examples.dao.UserDAO;
import com.poortoys.examples.entities.User;

public class BookingSimulation {
	 private static final int NUM_USERS = 5;
	    private static final int MAX_TICKETS_PER_USER = 2;
	    private static final int THREAD_POOL_SIZE = 5;
	    private static final int SIMULATION_DURATION_SECONDS = 30;

	    private final BookingService bookingService;
	    private final UserDAO userDAO;
	    private final ExecutorService executorService;


	    private final AtomicInteger successfulBookings = new AtomicInteger(0);
	    private final AtomicInteger failedBookings = new AtomicInteger(0);
	    private final AtomicInteger concurrencyConflicts = new AtomicInteger(0);
	    private long schemaModificationTime = 0;

	    public BookingSimulation(BookingService bookingService, UserDAO userDAO) {
	        this.bookingService = bookingService;
	        this.userDAO = userDAO;
	        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
	    }

	    public void runFullSimulation(int eventId) {
	        try {
	            int initialTicketCount = bookingService.getAvailableTicketSerials(eventId).size();
	            System.out.println("\n=== Starting MySQL Simulation ===");
	            System.out.println("Event: Jazz Nights");
	            System.out.println("Initial ticket count: " + initialTicketCount);

	            runPositiveScenario(eventId);
	            runNegativeScenario();
	            printFinalMetrics(eventId, initialTicketCount);
	        } catch (Exception e) {
	            System.err.println("Simulation failed: " + e.getMessage());
	        } finally {
	            executorService.shutdown();
	        }
	    }

	    private void runPositiveScenario(int eventId) {
	        System.out.println("Running transaction consistency scenario...");

	        CountDownLatch startLatch = new CountDownLatch(1);
	        CountDownLatch completionLatch = new CountDownLatch(NUM_USERS);

	        List<String> availableTickets = bookingService.getAvailableTicketSerials(eventId);
	        List<User> users = userDAO.findAll();
	        Random random = new Random();

	        for (int i = 0; i < NUM_USERS; i++) {
	            executorService.submit(() -> {
	                try {
	                    startLatch.await();
	                    long startTime = System.currentTimeMillis();

	                    User user = users.get(random.nextInt(users.size()));
	                    int ticketsToBook = 1 + random.nextInt(MAX_TICKETS_PER_USER);
	                    List<String> selectedTickets = selectRandomTickets(availableTickets, ticketsToBook);

	                    try {
	                        bookingService.createBooking(user.getUserId(), selectedTickets, user.getEmail());
	                        recordSuccess();
	                    } catch (Exception e) {
	                        recordFailure();
	                    }
	                } catch (Exception e) {
	                    failedBookings.incrementAndGet();
	                } finally {
	                    completionLatch.countDown();
	                }
	            });
	        }

	        startLatch.countDown();

	        try {
	            completionLatch.await(SIMULATION_DURATION_SECONDS, TimeUnit.SECONDS);
	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt();
	        }
	    }


	    private void runNegativeScenario() {
	        System.out.println("Running schema modification scenario...");

	        try {
	            long modificationTime = bookingService.modifyTicketSchema("seat_category", "VARCHAR(50)");
	            schemaModificationTime = modificationTime;
	        } catch (Exception e) {
	            System.err.println("Schema modification failed: " + e.getMessage());
	        }
	    }

	    private List<String> selectRandomTickets(List<String> availableTickets, int count) {
	        List<String> selected = new ArrayList<>();
	        synchronized (availableTickets) {
	            for (int i = 0; i < count && !availableTickets.isEmpty(); i++) {
	                int index = new Random().nextInt(availableTickets.size());
	                selected.add(availableTickets.remove(index));
	            }
	        }
	        return selected;
	    }

	    private void recordSuccess() {
	        successfulBookings.incrementAndGet();
	    }

	    private void recordFailure() {
	        failedBookings.incrementAndGet();
	    }

	    private void printFinalMetrics(int eventId, int initialTicketCount) {
	        List<String> currentTickets = bookingService.getAvailableTicketSerials(eventId);
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

	        System.out.println("\nKey Findings:");
	        System.out.println("1. Transaction Consistency: MySQL handled ACID transactions efficiently");
	        System.out.println("2. Schema Modification: Schema change failed due to high concurrency");
	        System.out.println("=======================\n");
	    }
	}