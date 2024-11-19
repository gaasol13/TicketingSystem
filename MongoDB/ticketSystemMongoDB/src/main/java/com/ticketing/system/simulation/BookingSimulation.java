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

public class BookingSimulation {
	
	// Standardized test parameters
    private static final int NUM_USERS = 1000;
    private static final int MAX_TICKETS_PER_USER = 2;
    private static final int THREAD_POOL_SIZE = 10;
    private static final int SIMULATION_DURATION_SECONDS = 30;
    private static final String DATABASE_TYPE = "MongoDB";
    
    private final BookingService bookingService;
    private final UserDAO userDAO;
    private final EventDAO eventDAO;
    private final TicketDAO ticketDAO;
    private final Datastore datastore;
    private final ExecutorService executorService;
    
    @Data
    public class SimulationMetrics {
        private int totalAttempts = 0;
        private int successfulBookings = 0;
        private int failedBookings = 0;
        private long dynamicFieldUpdateTime = 0;
        private int concurrencyConflicts = 0;
        private List<Long> responseTimesMs = new ArrayList<>();
		public void setDynamicFieldUpdateTime(long l) {
			// TODO Auto-generated method stub
			
		}
		public String getDynamicFieldUpdateTime() {
			// TODO Auto-generated method stub
			return null;
		}
		public int getSuccessfulBookings() {
			// TODO Auto-generated method stub
			return 0;
		}
		public int getTotalAttempts() {
			// TODO Auto-generated method stub
			return 0;
		}
		public List<Long> getResponseTimesMs() {
			// TODO Auto-generated method stub
			return null;
		}
		public void setTotalAttempts(int i) {
			// TODO Auto-generated method stub
			
		}
		public void setSuccessfulBookings(int i) {
			// TODO Auto-generated method stub
			
		}
		public int getConcurrencyConflicts() {
			// TODO Auto-generated method stub
			return 0;
		}
    }

    private final SimulationMetrics metrics = new SimulationMetrics();

    public BookingSimulation(Datastore datastore, BookingDAO bookingDAO, 
                           UserDAO userDAO, EventDAO eventDAO, TicketDAO ticketDAO) {
        this.datastore = datastore;
        this.userDAO = userDAO;
        this.eventDAO = eventDAO;
        this.ticketDAO = ticketDAO;
        this.bookingService = new BookingService(bookingDAO, ticketDAO, userDAO, eventDAO, datastore);
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    public void runSimulation(ObjectId eventId, int numUsers, int maxTicketsPerUser) {
        try {
            Event event = eventDAO.findById(eventId);
            if (event == null) {
                throw new RuntimeException("Event not found: " + eventId);
            }

            System.out.println("\n=== Starting MongoDB Simulation ===");
            System.out.println("Event: " + event.getName());
            System.out.println("Initial ticket count: " + ticketDAO.countAvailableTickets(eventId));

            // POSITIVE SCENARIO: Test dynamic ticket creation
            System.out.println("\nPOSITIVE SCENARIO - Testing Document Flexibility");
            runDynamicTicketScenario(eventId);

            // NEGATIVE SCENARIO: Test concurrent bookings
            System.out.println("\nNEGATIVE SCENARIO - Testing Concurrency Handling");
            runConcurrentBookingScenario(eventId, numUsers, maxTicketsPerUser);

            // Print final results
            printResults(eventId, event.getName());

        } catch (Exception e) {
            System.err.println("Simulation failed: " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    private void runDynamicTicketScenario(ObjectId eventId) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Test creating tickets with dynamic fields
            Map<String, Object> dynamicFields = new HashMap<>();
            dynamicFields.put("vipAccess", true);
            dynamicFields.put("merchandiseIncluded", Arrays.asList("t-shirt", "poster"));
            dynamicFields.put("specialOffer", new Document()
                .append("discount", 20)
                .append("validUntil", new Date()));

            boolean success = bookingService.createDynamicTicket(eventId, dynamicFields);
            
            metrics.setDynamicFieldUpdateTime(System.currentTimeMillis() - startTime);
            System.out.println("\nDynamic Ticket Creation Results:");
            System.out.println("- Status: " + (success ? "Successful" : "Failed"));
            System.out.println("- Processing time: " + metrics.getDynamicFieldUpdateTime() + "ms");
            System.out.println("- Fields added: " + dynamicFields.size());
            
        } catch (Exception e) {
            System.err.println("Dynamic ticket scenario failed: " + e.getMessage());
            metrics.setDynamicFieldUpdateTime(System.currentTimeMillis() - startTime);
        }
    }

    private void runConcurrentBookingScenario(ObjectId eventId, int numUsers, int maxTicketsPerUser) {
        List<User> users = userDAO.findAll();
        if (users.size() < numUsers) {
            System.out.println("Warning: Only " + users.size() + " users available for simulation");
            numUsers = users.size();
        }

        List<Callable<Boolean>> tasks = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < numUsers; i++) {
            final User user = users.get(random.nextInt(users.size()));
            final int ticketsToBook = random.nextInt(maxTicketsPerUser) + 1;

            tasks.add(() -> {
                metrics.setTotalAttempts(metrics.getTotalAttempts() + 1);
                long startTime = System.currentTimeMillis();
                boolean success = bookingService.bookTickets(user.getId(), eventId, ticketsToBook);
                metrics.getResponseTimesMs().add(System.currentTimeMillis() - startTime);
                
                if (success) metrics.setSuccessfulBookings(metrics.getSuccessfulBookings() + 1);
                else metrics.setSuccessfulBookings(metrics.getSuccessfulBookings() + 1);
                
                return success;
            });
        }

        try {
            executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void printResults(ObjectId eventId, String eventName) {
        double avgResponseTime = metrics.getResponseTimesMs().stream()
            .mapToLong(Long::valueOf)
            .average()
            .orElse(0.0);

        System.out.println("\n=== MongoDB Simulation Results ===");
        System.out.println("Event: " + eventName);
        
        System.out.println("\nPOSITIVE SCENARIO Results:");
        System.out.println("- Dynamic field update time: " + metrics.getDynamicFieldUpdateTime() + "ms");
        System.out.println("- Dynamic fields processed: " + bookingService.getDynamicFieldUpdates());
        
        System.out.println("\nNEGATIVE SCENARIO Results:");
        System.out.println("- Total booking attempts: " + metrics.getTotalAttempts());
        System.out.println("- Successful bookings: " + metrics.getSuccessfulBookings());
        System.out.println("- Failed bookings: " + metrics.getSuccessfulBookings());
        System.out.println("- Concurrency conflicts: " + bookingService.getConcurrencyConflicts());
        System.out.println("- Average response time: " + String.format("%.2f", avgResponseTime) + "ms");
        System.out.println("- Remaining tickets: " + ticketDAO.countAvailableTickets(eventId));
        
        System.out.println("\nKey Findings:");
        System.out.println("1. Document Flexibility: MongoDB handled dynamic fields efficiently");
        System.out.println("2. Concurrency Challenges: " + 
            String.format("%.1f%%", (double)bookingService.getConcurrencyConflicts() / 
            metrics.getTotalAttempts() * 100) + " conflict rate");
        System.out.println("=======================\n");
    }

    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                    System.out.println("Forcing shutdown of executor service...");
                }
                System.out.println("Simulation executor service shutdown complete.");
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
                System.err.println("Simulation shutdown interrupted: " + e.getMessage());
            }
        }
    }

    // Helper methods for metric access
    public SimulationMetrics getMetrics() {
        return metrics;
    }

    public int getTotalAttempts() {
        return metrics.getTotalAttempts();
    }

    public int getSuccessfulBookings() {
        return metrics.getSuccessfulBookings();
    }

    public int getFailedBookings() {
        return metrics.getSuccessfulBookings();
    }

    public int getConcurrencyConflicts() {
        return metrics.getConcurrencyConflicts();
    }

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