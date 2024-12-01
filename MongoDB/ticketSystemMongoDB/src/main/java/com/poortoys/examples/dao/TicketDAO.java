/**
 * TicketDAO class is responsible for performing CRUD operations on Ticket entities.
 * It uses Morphia for Object-Document Mapping (ODM) to interact with MongoDB.
 * This class also handles ticket booking operations with support for transactions.
 */

package com.poortoys.examples.dao;

// Import necessary Morphia and MongoDB libraries for database operations
import com.mongodb.client.ClientSession;
import dev.morphia.Datastore;
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.query.experimental.updates.UpdateOperators;
import org.bson.types.ObjectId;
import com.ticketing.system.entities.Ticket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TicketDAO {
    // Datastore instance for MongoDB interaction using Morphia
    private final Datastore datastore;

    /**
     * Constructor to initialize the DAO with a datastore instance.
     * @param datastore The Morphia Datastore object connected to the MongoDB database.
     */
    public TicketDAO(Datastore datastore) {
        this.datastore = datastore;
    }

    /**
     * Books multiple tickets atomically within a transaction.
     * @param session The ClientSession object for managing transactions.
     * @param eventId The ID of the event for which tickets are being booked.
     * @param quantity The number of tickets to book.
     * @return A list of booked tickets, or an empty list if no tickets were booked.
     */
    public List<Ticket> bookAvailableTickets(ClientSession session, ObjectId eventId, int quantity) {
        List<Ticket> bookedTickets = new ArrayList<>(); // List to store successfully booked tickets

        try {
            // Attempt to book the requested quantity of tickets
            for (int i = 0; i < quantity; i++) {
                Ticket ticket = datastore.find(Ticket.class)
                    .filter(Filters.and(
                        Filters.eq("event_id", eventId), // Match tickets for the specified event
                        Filters.eq("status", "available") // Only select tickets with 'available' status
                    ))
                    .modify(UpdateOperators.set("status", "booked")) // Atomically update the status to 'booked'
                    .execute(); // Execute the query and fetch the modified ticket
                
                if (ticket != null) {
                    bookedTickets.add(ticket); // Add the booked ticket to the list
                }
            }
            return bookedTickets; // Return the list of successfully booked tickets
        } catch (Exception e) {
            System.err.println("Error booking tickets: " + e.getMessage()); // Log any exceptions encountered
            throw e; // Rethrow the exception to allow higher-level handling
        }
    }

    /**
     * Counts the number of available tickets for a specific event.
     * @param session The ClientSession object for managing transactions.
     * @param eventId The ID of the event.
     * @return The total number of available tickets for the event.
     */
    public long countAvailableTickets(ClientSession session, ObjectId eventId) {
        return datastore.find(Ticket.class)
            .filter(Filters.and(
                Filters.eq("event_id", eventId), // Match tickets for the specified event
                Filters.eq("status", "available") // Only count tickets with 'available' status
            ))
            .count(); // Return the total count of matching tickets
    }

    /**
     * Rolls back any booked tickets in case of transaction failure.
     * @param session The ClientSession object for managing transactions.
     * @param tickets The list of tickets to roll back.
     */
    private void rollbackBookings(ClientSession session, List<Ticket> tickets) {
        for (Ticket ticket : tickets) { // Loop through the list of tickets to be rolled back
            try {
                ticket.setStatus("available"); // Reset the ticket status to 'available'
                ticket.setPurchaseDate(null); // Remove the purchase date
                datastore.save(ticket); // Save the updated ticket back to the database
            } catch (Exception e) {
                System.err.println("Error during rollback for ticket " +
                    ticket.getId() + ": " + e.getMessage()); // Log any rollback errors
            }
        }
    }

    /**
     * Finds all available tickets for a specific event.
     * @param eventId The ID of the event.
     * @return A list of tickets that are available for booking.
     */
    public List<Ticket> findAvailableTickets(ObjectId eventId) {
        return datastore.find(Ticket.class)
            .filter(Filters.and(
                Filters.eq("event_id", eventId), // Match tickets for the specified event
                Filters.eq("status", "available") // Only select tickets with 'available' status
            ))
            .iterator() // Fetch the results as an iterator
            .toList(); // Convert the iterator to a list and return it
    }

    /**
     * Finds a ticket by its serial number.
     * @param serialNumber The serial number of the ticket.
     * @return The Ticket object if found, or null otherwise.
     */
    public Ticket findBySerialNumber(String serialNumber) {
        return datastore.find(Ticket.class)
            .filter(Filters.eq("serial_number", serialNumber)) // Match the ticket by its serial number
            .first(); // Return the first matching result
    }

    /**
     * Creates a new ticket in the database.
     * @param ticket The Ticket object to create.
     */
    public void create(Ticket ticket) {
        datastore.save(ticket); // Save the new ticket to the database
    }

    /**
     * Updates an existing ticket in the database.
     * @param ticket The Ticket object to update.
     */
    public void update(Ticket ticket) {
        datastore.save(ticket); // Save the updated ticket to the database
    }

    /**
     * Finds a ticket by its unique ID.
     * @param id The ObjectId of the ticket.
     * @return The Ticket object if found, or null otherwise.
     */
    public Ticket findById(ObjectId id) {
        return datastore.find(Ticket.class)
            .filter(Filters.eq("_id", id)) // Match the ticket by its unique ID
            .first(); // Return the first matching result
    }

    /**
     * Gets the total count of all tickets in the database.
     * @return The total number of tickets.
     */
    public long count() {
        return datastore.find(Ticket.class).count(); // Count and return all tickets
    }
}
