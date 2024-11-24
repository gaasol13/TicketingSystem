package com.ticketing.system.simulation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.poortoys.examples.dao.BookingDAO;
import com.poortoys.examples.dao.EventDAO;
import com.poortoys.examples.dao.TicketDAO;
import com.poortoys.examples.dao.UserDAO;
import com.ticketing.system.entities.Booking;
import com.ticketing.system.entities.Ticket;
import com.ticketing.system.entities.User;
import com.ticketing.system.entities.Event;

import dev.morphia.Datastore;

public class BookingService {
	
	 private long totalQueryTime = 0;
	    private int totalQueries = 0;
	    // Metrics
	    private AtomicInteger successfulBookings = new AtomicInteger(0);
	    private AtomicInteger failedBookings = new AtomicInteger(0);
	    private AtomicInteger dynamicFieldUpdates = new AtomicInteger(0);   
	    private AtomicInteger totalTicketsBooked = new AtomicInteger(0);
	
	private final BookingDAO bookingDAO;
    private final TicketDAO ticketDAO;
    private final UserDAO userDAO;
    private final EventDAO eventDAO;
    private final Datastore datastore;
    
   ;

    public BookingService(BookingDAO bookingDAO, TicketDAO ticketDAO, 
    		UserDAO userDAO, EventDAO eventDAO, Datastore datastore) {
        this.bookingDAO = bookingDAO;
        this.ticketDAO = ticketDAO;
        this.userDAO = userDAO;
        this.eventDAO = eventDAO;
        this.datastore = datastore;
    }

    public boolean bookTickets(ObjectId userId, ObjectId eventId, int quantity) {
        System.out.println("User " + userId + " attempting to book "
                + quantity + " tickets for event " + eventId);

        // Start a session for transaction
        try (ClientSession session = datastore.startSession()) {
            session.startTransaction();

            long queryStartTime = System.nanoTime(); // Start timing the transaction
            try {
                // Measure query time for fetching the event
                long startTime = System.nanoTime();
                Event event = eventDAO.findById(eventId);
                long endTime = System.nanoTime();
                totalQueryTime += (endTime - startTime);
                totalQueries++;

                if (event == null) {
                    System.out.println("Event not found: " + eventId);
                    failedBookings.incrementAndGet();
                    session.abortTransaction();
                    return false;
                }

                // Measure query time for counting available tickets
                startTime = System.nanoTime();
                long availableTickets = ticketDAO.countAvailableTickets(eventId);
                endTime = System.nanoTime();
                totalQueryTime += (endTime - startTime);
                totalQueries++;

                System.out.println("\nAvailable tickets: " + availableTickets);

                if (availableTickets < quantity) {
                    System.out.println("\nNot enough tickets available for event: " + eventId);
                    failedBookings.incrementAndGet();
                    session.abortTransaction();
                    return false;
                }

                // Measure query time for booking tickets
                startTime = System.nanoTime();
                List<Ticket> bookedTickets = ticketDAO.bookAvailableTickets(session, eventId, quantity);
                endTime = System.nanoTime();
                totalQueryTime += (endTime - startTime);
                totalQueries++;

                System.out.println("\nTickets booked: " + bookedTickets.size());

                if (bookedTickets.size() < quantity) {
                    System.out.println("Failed to book the desired number of tickets. Requested: " + quantity + ", Booked: \n"
                            + bookedTickets.size());
                    failedBookings.incrementAndGet();
                    session.abortTransaction();
                    return false;
                }

                // Calculate total price
                BigDecimal totalPrice = calculateTotalPrice(event, bookedTickets);
                System.out.println("\nTotal price for booking: \n" + totalPrice);

                // Measure query time for fetching user email
                startTime = System.nanoTime();
                String userEmail = getUserEmail(userId);
                endTime = System.nanoTime();
                totalQueryTime += (endTime - startTime);
                totalQueries++;

                // Create a new booking
                Booking booking = new Booking(
                        userId,
                        eventId,
                        userEmail,
                        new Date(), // deliveryTime
                        new Date(), // timePaid
                        new Date(), // timeSent
                        totalPrice,
                        BigDecimal.ZERO, // discount
                        totalPrice, // finalPrice
                        "confirmed",
                        extractTicketIds(bookedTickets)
                );

                // Measure query time for saving the booking
                startTime = System.nanoTime();
                bookingDAO.create(booking);
                endTime = System.nanoTime();
                totalQueryTime += (endTime - startTime);
                totalQueries++;

                System.out.println("Booking created: " + booking);

                // Commit the transaction
                session.commitTransaction();
                successfulBookings.incrementAndGet();
                
                // Update total tickets booked
                totalTicketsBooked.addAndGet(bookedTickets.size());
                
                
                System.out.println("Booking successful for user " + userId + ". Total successful bookings: \n" + successfulBookings.get());
                return true;

            } catch (Exception e) {
                System.err.println("Error during booking: " + e.getMessage());
                e.printStackTrace();
                failedBookings.incrementAndGet();
                System.out.println("Booking failed for user " + userId + ". Total failed bookings: " + failedBookings.get());
                session.abortTransaction();
                return false;
            } finally {
                long queryEndTime = System.nanoTime();
                totalQueryTime += (queryEndTime - queryStartTime);
                totalQueries++;
            }
        }
    }
    
    
    
    // New method to get total tickets booked
    public int getTotalTicketsBooked() {
        return totalTicketsBooked.get();
    }

    /**
     * Calculates the total price for the booked tickets based on their categories.
     */
    private BigDecimal calculateTotalPrice(Event event, List<Ticket> bookedTickets) {
        BigDecimal total = BigDecimal.ZERO;
        for (Ticket ticket : bookedTickets) {
            // Retrieve the price from the event's ticket categories
            BigDecimal price = getPriceForCategory(event, ticket.getTicketCategory());
            total = total.add(price);
        }
        return total;
    }

    /**
     * Retrieves the price for a given ticket category.
     */
    private BigDecimal getPriceForCategory(Event event, String ticketCategory) {
        return event.getTicketCategories().stream()
                .filter(tc -> tc.getDescription().equalsIgnoreCase(ticketCategory))
                .map(tc -> tc.getPrice())
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }
    /**
     * POSITIVE SCENARIO: Demonstrates MongoDB's flexible document structure
     * by handling dynamic ticket categories and pricing.
     */
    public boolean createDynamicTicket(ObjectId eventId, Map<String, Object> dynamicFields) {
        try {
            long startTime = System.nanoTime(); // Record start time

            // Create a base ticket document with fixed fields
            Document ticketDoc = new Document()
                .append("eventId", eventId)
                .append("status", "AVAILABLE")
                .append("created", new Date());

            // Dynamically add custom fields to the ticket document
            dynamicFields.forEach((key, value) -> {
                ticketDoc.append(key, value);
                dynamicFieldUpdates.incrementAndGet(); // Increment dynamic field updates counter
            });

            // Obtain the "tickets" collection directly from the datastore
            MongoCollection<Document> collection = datastore.getDatabase().getCollection("tickets");

            // Insert the ticket document into the collection
            collection.insertOne(ticketDoc);

            long endTime = System.currentTimeMillis(); // Record end time
            totalQueryTime += (endTime - startTime) * 1_000_000; // Convert ms to ns
            totalQueries++;

            System.out.println("Successfully created dynamic ticket with " +
                dynamicFields.size() + " custom fields");
            System.out.println("Operation time: " + (endTime - startTime) + "ms");
            return true;
        } catch (Exception e) {
            System.err.println("Failed to create dynamic ticket: " + e.getMessage());
            return false;
        }
    }
    /**
     * Retrieves the email of a user by their ID.
     */
    private String getUserEmail(ObjectId userId) {
        User user = userDAO.findById(userId);
        return user != null ? user.getEmail() : "unknown@example.com";
    }

    /**
     * Extracts ticket IDs from a list of Ticket objects.
     */
    private List<ObjectId> extractTicketIds(List<Ticket> tickets) {
        List<ObjectId> ticketIds = new ArrayList<>();
        for (Ticket ticket : tickets) {
            ticketIds.add(ticket.getId());
        }
        return ticketIds;
    }

    /**
     * Retrieves the number of successful bookings.
     */
    public int getSuccessfulBookings() {
        return successfulBookings.get();
    }

    /**
     * Retrieves the number of failed bookings.
     */
    public int getFailedBookings() {
        return failedBookings.get();
    }

    public double getAverageQueryTime() {
        return totalQueries > 0 ? (double) totalQueryTime / totalQueries / 1_000_000 : 0; // ms
    }

    public int getTotalQueries() {
        return totalQueries;
    }

    public int getDynamicFieldUpdates() {
        return dynamicFieldUpdates.get();
    }
}