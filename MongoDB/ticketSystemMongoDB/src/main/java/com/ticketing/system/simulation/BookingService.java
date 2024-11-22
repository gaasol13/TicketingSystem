package com.ticketing.system.simulation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.poortoys.examples.dao.BookingDAO;
import com.poortoys.examples.dao.EventDAO;
import com.poortoys.examples.dao.TicketDAO;
import com.poortoys.examples.dao.UserDAO;
import com.ticketing.system.entities.Booking;
import com.ticketing.system.entities.Event;
import com.ticketing.system.entities.Ticket;
import com.ticketing.system.entities.User;

import dev.morphia.Datastore;

/**
 * service class handling ticket booking operations using MongoDB.
 * demonstrates MongoDB's approach to:
 * 1. document-based transactions
 * 2. schema flexibility
 * 3. atomic operations for concurrency
 */
public class BookingService {

    // DAOs for interacting with different database collections
    private final BookingDAO bookingDAO;
    private final TicketDAO ticketDAO;
    private final UserDAO userDAO;
    private final EventDAO eventDAO;
    private final Datastore datastore;

    //Thread-safe counters for monitoring concurrent operations
    private AtomicInteger successfulBookings = new AtomicInteger(0);        //ounter for successful bookings
    private AtomicInteger failedBookings = new AtomicInteger(0);            //counter for failed bookings
    private AtomicInteger concurrencyConflicts = new AtomicInteger(0);      //counter for concurrency conflicts
    private AtomicInteger dynamicFieldUpdates = new AtomicInteger(0);       //counter for dynamic field updates

    /**
     * constructor for BookingService that initializes DAOs and the datastore.
     */
    public BookingService(BookingDAO bookingDAO, TicketDAO ticketDAO,
                          UserDAO userDAO, EventDAO eventDAO, Datastore datastore) {
        this.bookingDAO = bookingDAO;
        this.ticketDAO = ticketDAO;
        this.userDAO = userDAO;
        this.eventDAO = eventDAO;
        this.datastore = datastore;
    }

    /**
     * POSITIVE SCENARIO: demonstrates MongoDB's flexible document structure
     * by handling dynamic ticket categories and pricing.
     */
    public boolean createDynamicTicket(ObjectId eventId, Map<String, Object> dynamicFields) {
        try {
            long startTime = System.currentTimeMillis(); //record the start time of the operation

            // create a base ticket document with fixed fields
            Document ticketDoc = new Document()
                .append("eventId", eventId)           //id of the associated event
                .append("status", "AVAILABLE")        //initial status of the ticket
                .append("created", new Date());       //creation date of the ticket

            //dynamically add custom fields to the ticket document
            dynamicFields.forEach((key, value) -> {
                ticketDoc.append(key, value);        //add each key-value pair to the document
                dynamicFieldUpdates.incrementAndGet(); // increment the dynamic field updates counter
            });

            //obtain the "tickets" collection directly from the datastore
            MongoCollection<Document> collection = datastore.getDatabase().getCollection("tickets");

            //insert the ticket document into the collection
            collection.insertOne(ticketDoc);

            //print success messages to the console
            System.out.println("Successfully created dynamic ticket with " +
                dynamicFields.size() + " custom fields");
            System.out.println("Operation time: " + (System.currentTimeMillis() - startTime) + "ms");
            return true; // indicate that the operation was successful
        } catch (Exception e) {
            // handle exceptions in case of failure during ticket creation
            System.err.println("Failed to create dynamic ticket: " + e.getMessage());
            return false; // indicate that the operation failed
        }
    }

    /**
     * NEGATIVE SCENARIO: shows MongoDB's concurrency challenges during ticket booking
     * - client sessions for transactions
     * - document-level atomicity
     * - eventual consistency model
     */
    public boolean bookTickets(ObjectId userId, ObjectId eventId, int quantity) {
        long startTime = System.currentTimeMillis(); // record the start time of the operation
        System.out.println("User " + userId + " attempting to book " + quantity + " tickets");

        //start a client session to handle the transaction
        try (ClientSession session = datastore.startSession()) {
            session.startTransaction(); // begin the transaction
            try {
                // 1. validate user existence and check ticket availability
                User user = userDAO.findById(userId); // find the user by id
                if (user == null) {
                    recordFailure("User not found", startTime); //ecord failure if user does not exist
                    return false;
                }

                Event event = eventDAO.findById(eventId); //find the event by id
                if (event == null) {
                    recordFailure("Event not found", startTime); //record failure if event does not exist
                    return false;
                }

                //2. check ticket availability (potential race condition point)
                List<Ticket> availableTickets = ticketDAO.findAvailableTicketsByEventId(eventId);
                if (availableTickets.size() < quantity) {
                    recordFailure("Insufficient tickets", startTime); // record failure if not enough tickets
                    return false;
                }

                // 3. attempt to lock tickets (demonstration of concurrency challenges)
                List<Ticket> lockedTickets = new ArrayList<>();
                for (int i = 0; i < quantity; i++) {
                    try {
                        Ticket ticket = availableTickets.get(i); // get an available ticket
                        // attempt to atomically change the ticket status to "LOCKED"
                        Ticket lockedTicket = ticketDAO.findAndModifyTicket(session, ticket.getId(), "LOCKED");
                        if (lockedTicket == null) {
                            concurrencyConflicts.incrementAndGet(); // increment the concurrency conflicts counter
                            throw new RuntimeException("Concurrent access detected for ticket: " + ticket.getId());
                        }
                        lockedTickets.add(lockedTicket); // add the locked ticket to the list
                    } catch (Exception e) {
                        session.abortTransaction(); // abort the transaction in case of error
                        recordFailure("Concurrent access conflict", startTime); // record failure due to conflict
                        return false;
                    }
                }

                // 4. create booking if ticket locking was successful
                BigDecimal totalPrice = calculateTotalPrice(event, lockedTickets); // calculate total price
                Booking booking = createBooking(user, event, lockedTickets, totalPrice); // create the booking entity
                bookingDAO.create(session, booking); // persist the booking in the database

                // 5. update ticket statuses to "SOLD" (another consistency challenge)
                for (Ticket ticket : lockedTickets) {
                    ticketDAO.update(session, ticket.getId(), "SOLD"); // update the ticket status
                }

                session.commitTransaction(); // commit the transaction
                recordSuccess("Booking completed successfully", startTime); // record successful booking
                return true; // indicate that the booking was successful

            } catch (Exception e) {
                session.abortTransaction(); // abort the transaction in case of any exception
                recordFailure("Transaction failed: " + e.getMessage(), startTime); // record the failure reason
                return false; // indicate that the booking failed
            }
        }
    }

    /**
     * helper method to record a successful booking
     */
    private void recordSuccess(String message, long startTime) {
        successfulBookings.incrementAndGet(); // increment the successful bookings counter
        long duration = System.currentTimeMillis() - startTime; // calculate the operation duration
        System.out.println("✓ " + message + " (Duration: " + duration + "ms)"); // print success message
    }

    /**
     *  method to record a failed booking
     */
    private void recordFailure(String reason, long startTime) {
        failedBookings.incrementAndGet(); // increment the failed bookings counter
        long duration = System.currentTimeMillis() - startTime; // calculate the operation duration
        System.out.println("✗ Failed: " + reason + " (Duration: " + duration + "ms)"); // print failure message
    }

    /**
     *  method to calculate the total price of a booking
     */
    private BigDecimal calculateTotalPrice(Event event, List<Ticket> tickets) {
        return tickets.stream()
            .map(ticket -> getPriceForCategory(event, ticket.getTicketCategory())) // get price per category
            .reduce(BigDecimal.ZERO, BigDecimal::add); // sum all prices
    }

    /**
     *  method to get the price for a specific ticket category
     */
    private BigDecimal getPriceForCategory(Event event, String category) {
        return event.getTicketCategories().stream()
            .filter(tc -> tc.getDescription().equalsIgnoreCase(category)) // filter by category description
            .map(tc -> tc.getPrice()) // map to prices
            .findFirst() // get the first matching category
            .orElse(BigDecimal.ZERO); // return 0 if category not found
    }

    /**
     * helper method to create a booking entity.
     */
    private Booking createBooking(User user, Event event, List<Ticket> tickets, BigDecimal totalPrice) {
        BigDecimal discount = calculateDiscount(user, totalPrice); // calculate applicable discount
        return new Booking(
            user.getId(),                                         // user id
            event.getId(),                                        // event id
            user.getEmail(),                                      // user email
            new Date(),                                           // booking creation date
            new Date(),                                           // booking modification date
            new Date(),                                           // booking confirmation date
            totalPrice,                                           // total price before discounts
            discount,                                             // applied discount
            totalPrice.subtract(discount),                        // final price after discounts
            "CONFIRMED",                                          // booking status
            tickets.stream().map(Ticket::getId).collect(Collectors.toList()) // list of booked ticket ids
        );
    }

    /**
     * method to calculate the discount applicable to a booking.
     */
    private BigDecimal calculateDiscount(User user, BigDecimal totalPrice) {
        // apply a 5% discount if the user has made more than 5 bookings
        return bookingDAO.countBookingsByUser(user.getId()) > 5
            ? totalPrice.multiply(new BigDecimal("0.05"))
            : BigDecimal.ZERO;
    }
    
    /**
     * retrieves the total number of successful bookings.
     */
    public int getSuccessfulBookings() { 
    	return successfulBookings.get(); }

    /**
     * number of failed bookings.
     */
    public int getFailedBookings() { 
    	return failedBookings.get(); }

    /**
     *  total number of detected concurrency conflicts.
     */
    public int getConcurrencyConflicts() { 
    	return concurrencyConflicts.get(); }

    /**
     *  total number of dynamic field updates performed.
     */
    public int getDynamicFieldUpdates() { 
    	return dynamicFieldUpdates.get(); }
}
